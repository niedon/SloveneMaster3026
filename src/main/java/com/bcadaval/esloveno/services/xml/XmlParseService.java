package com.bcadaval.esloveno.services.xml;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.bcadaval.esloveno.beans.enums.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.bcadaval.esloveno.beans.base.Palabra;
import com.bcadaval.esloveno.beans.palabra.Adjetivo;
import com.bcadaval.esloveno.beans.palabra.AdjetivoFlexion;
import com.bcadaval.esloveno.beans.palabra.Numeral;
import com.bcadaval.esloveno.beans.palabra.NumeralFlexion;
import com.bcadaval.esloveno.beans.palabra.Pronombre;
import com.bcadaval.esloveno.beans.palabra.PronombreFlexion;
import com.bcadaval.esloveno.beans.palabra.Sustantivo;
import com.bcadaval.esloveno.beans.palabra.SustantivoFlexion;
import com.bcadaval.esloveno.beans.palabra.Verbo;
import com.bcadaval.esloveno.beans.palabra.VerboFlexion;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class XmlParseService {

    @Value("${app.xml.path:/data/xml}")
    private String xmlPath;

	// =========================================================================
	// CONSTANTES XPATH
	// =========================================================================

	private static final String XPATH_FLEXION = "formRepresentations/orthographyList/orthography/form/text()";
	private static final String XPATH_ACENTUADO = "formRepresentations/accentuationList/accentuation/form/text()";
	private static final String XPATH_PRONUNCIACION_IPA = "formRepresentations/pronunciationList/pronunciation/form[@script='IPA']/text()";
	private static final String XPATH_PRONUNCIACION_SAMPA = "formRepresentations/pronunciationList/pronunciation/form[@script='SAMPA']/text()";

	// XPaths para grammarFeatures
	private static final String XPATH_GENDER = "grammarFeatureList/grammarFeature[@name='gender']/text()";
	private static final String XPATH_NUMBER = "grammarFeatureList/grammarFeature[@name='number']/text()";
	private static final String XPATH_CASE = "grammarFeatureList/grammarFeature[@name='case']/text()";
	private static final String XPATH_PERSON = "grammarFeatureList/grammarFeature[@name='person']/text()";
	private static final String XPATH_DEGREE = "grammarFeatureList/grammarFeature[@name='degree']/text()";
	private static final String XPATH_VFORM = "grammarFeatureList/grammarFeature[@name='vform']/text()";
	private static final String XPATH_CLITIC = "grammarFeatureList/grammarFeature[@name='clitic']/text()";
	private static final String XPATH_DEFINITENESS = "grammarFeatureList/grammarFeature[@name='definiteness']/text()";

	// XPaths para head
	private static final String XPATH_SLOLEKS_ID = "/entry/head/lexicalUnit/@sloleksId";
	private static final String XPATH_SLOLEKS_KEY = "/entry/head/lexicalUnit/@sloleksKey";
	private static final String XPATH_CATEGORY = "/entry/head/grammar/category/text()";
	private static final String XPATH_LEMMA = "/entry/head/headword/lemma/text()";
	private static final String XPATH_HEAD_GENDER = "/entry/head/grammar/grammarFeature[@name='gender']";
	private static final String XPATH_HEAD_ASPECT = "/entry/head/grammar/grammarFeature[@name='aspect']";
	private static final String XPATH_HEAD_TYPE = "/entry/head/grammar/grammarFeature[@name='type']";

	// XPaths para wordForms
	private static final String XPATH_WORDFORMS = "/entry/body/wordFormList/wordForm";

    /**
     * DTO para resultados de búsqueda con tipo
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class ResultadoBusqueda {
        private String lema;
        private String tipo;
        private String tipoEspanol;
        private boolean soportado;
        private String xmlContent;
    }

    /**
     * Busca TODAS las entradas con el lema dado y devuelve lista de resultados
     */
    public List<ResultadoBusqueda> buscarTodas(String word) throws XmlParserException {
        Instant inicio = Instant.now();
        try {
            List<ResultadoBusqueda> resultados = getAllXmlStrings(word);
            log.info("Encontradas {} entradas para '{}'", resultados.size(), word);
            return resultados;
        } catch (IOException e) {
            throw new XmlParserException("Error buscando palabra: " + e.getMessage(), e);
        } finally {
            log.debug("buscarTodas() - Duración: {}", Duration.between(inicio, Instant.now()));
        }
    }

    /**
     * Parsea una palabra específica dado su XML
     */
    public Palabra<?> parsearDesdeXml(String xmlContent) throws XmlParserException {
        try {
            Document doc = parseXmlString(xmlContent);
            XPath xPath = XPathFactory.newInstance().newXPath();
            String category = getXPathValue(doc, xPath, XPATH_CATEGORY);
            String lema = getXPathValue(doc, xPath, XPATH_LEMMA);

			return switch (TipoPalabra.fromXmlCode(category)){
					case SUSTANTIVO -> parseSustantivo(doc, xPath, lema);
					case VERBO -> parseVerbo(doc, xPath, lema);
					case ADJETIVO -> parseAdjetivo(doc, xPath, lema);
					case PRONOMBRE -> parsePronombre(doc, xPath, lema);
					case NUMERAL -> parseNumeral(doc, xPath, lema);
					default -> throw new XmlParserException("Tipo de palabra no soportada: " + category);
			};
        } catch (SAXException | IOException | ParserConfigurationException | XPathException e) {
            throw new XmlParserException("Error parseando XML: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // MÉTODOS DE PARSEO ESPECÍFICOS POR TIPO DE PALABRA
    // =========================================================================

    private Sustantivo parseSustantivo(Document doc, XPath xPath, String principal) throws XPathException {
        NodeList wordForms = (NodeList) xPath.compile(XPATH_WORDFORMS).evaluate(doc, XPathConstants.NODESET);
        return Sustantivo.builder()
                .principal(principal)
                .genero(Genero.fromCode(getXPathValue(doc, xPath, XPATH_HEAD_GENDER)))
                .sloleksId(getXPathValue(doc, xPath, XPATH_SLOLEKS_ID))
                .sloleksKey(getXPathValue(doc, xPath, XPATH_SLOLEKS_KEY))
                .listaFlexiones(
						IntStream.range(0, wordForms.getLength())
						.mapToObj(wordForms::item)
						.map(wordForm -> SustantivoFlexion.builder()
								.principal(principal)
								.flexion(getNodeXPathValue(wordForm, xPath, XPATH_FLEXION))
								.acentuado(getNodeXPathValue(wordForm, xPath, XPATH_ACENTUADO))
								.pronunciacionIpa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_IPA))
								.pronunciacionSampa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_SAMPA))
								.caso(Caso.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_CASE)))
								.numero(Numero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_NUMBER)))
								.build())
						.filter(Objects::nonNull)
						.filter(flexion -> StringUtils.isNotBlank(flexion.getFlexion()))
						.toList())
                .build();
    }

    private Verbo parseVerbo(Document doc, XPath xPath, String principal) throws XPathException {
        NodeList wordForms = (NodeList) xPath.compile(XPATH_WORDFORMS).evaluate(doc, XPathConstants.NODESET);
        return Verbo.builder()
                .principal(principal)
                .sloleksId(getXPathValue(doc, xPath, XPATH_SLOLEKS_ID))
                .sloleksKey(getXPathValue(doc, xPath, XPATH_SLOLEKS_KEY))
                .aspecto(Aspecto.fromCode(getXPathValue(doc, xPath, XPATH_HEAD_ASPECT)))
                .listaFlexiones(IntStream.range(0, wordForms.getLength())
						.mapToObj(wordForms::item)
						.map(wordForm -> VerboFlexion.builder()
								.principal(principal)
								.flexion(getNodeXPathValue(wordForm, xPath, XPATH_FLEXION))
								.acentuado(getNodeXPathValue(wordForm, xPath, XPATH_ACENTUADO))
								.pronunciacionIpa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_IPA))
								.pronunciacionSampa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_SAMPA))
								.formaVerbal(FormaVerbal.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_VFORM)))
								.persona(Persona.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_PERSON)))
								.numero(Numero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_NUMBER)))
								.genero(Genero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_GENDER)))
								.build())
						.filter(Objects::nonNull)
						.filter(flexion -> StringUtils.isNotBlank(flexion.getFlexion()))
						.toList())
                .build();
    }

    private Adjetivo parseAdjetivo(Document doc, XPath xPath, String principal) throws XPathException {
        NodeList wordForms = (NodeList) xPath.compile(XPATH_WORDFORMS).evaluate(doc, XPathConstants.NODESET);
        return Adjetivo.builder()
                .principal(principal)
                .sloleksId(getXPathValue(doc, xPath, XPATH_SLOLEKS_ID))
                .sloleksKey(getXPathValue(doc, xPath, XPATH_SLOLEKS_KEY))
                .listaFlexiones(IntStream.range(0, wordForms.getLength())
						.mapToObj(wordForms::item)
						.map(wordForm -> AdjetivoFlexion.builder()
								.principal(principal)
								.flexion(getNodeXPathValue(wordForm, xPath, XPATH_FLEXION))
								.acentuado(getNodeXPathValue(wordForm, xPath, XPATH_ACENTUADO))
								.pronunciacionIpa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_IPA))
								.pronunciacionSampa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_SAMPA))
								.genero(Genero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_GENDER)))
								.numero(Numero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_NUMBER)))
								.caso(Caso.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_CASE)))
								.grado(Grado.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_DEGREE)))
								.definitud(Definitud.fromCodigoXml(getNodeXPathValue(wordForm, xPath, XPATH_DEFINITENESS)))
								.build())
						.filter(Objects::nonNull)
						.filter(flexion -> StringUtils.isNotBlank(flexion.getFlexion()))
                        // Excluye la forma animada que está puesta en el xml como el orto
                        .filter(flexion -> !(flexion.getGrado() == Grado.POSITIVO &&
                                            flexion.getGenero() == Genero.MASCULINO &&
                                            flexion.getNumero() == Numero.SINGULAR &&
                                            flexion.getCaso() == Caso.ACUSATIVO &&
                                            flexion.getDefinitud() == null))
						.toList())
                .build();
    }

    private Pronombre parsePronombre(Document doc, XPath xPath, String principal) throws XPathException {
        NodeList wordForms = (NodeList) xPath.compile(XPATH_WORDFORMS).evaluate(doc, XPathConstants.NODESET);
        return Pronombre.builder()
                .principal(principal)
                .tipoPronombre(TipoPronombre.fromCode(getXPathValue(doc, xPath, XPATH_HEAD_TYPE)))
                .sloleksId(getXPathValue(doc, xPath, XPATH_SLOLEKS_ID))
                .sloleksKey(getXPathValue(doc, xPath, XPATH_SLOLEKS_KEY))
                .listaFlexiones(IntStream.range(0, wordForms.getLength())
						.mapToObj(wordForms::item)
						.map(wordForm -> PronombreFlexion.builder()
								.principal(principal)
								.flexion(getNodeXPathValue(wordForm, xPath, XPATH_FLEXION))
								.acentuado(getNodeXPathValue(wordForm, xPath, XPATH_ACENTUADO))
								.pronunciacionIpa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_IPA))
								.pronunciacionSampa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_SAMPA))
								.persona(Persona.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_PERSON)))
								.genero(Genero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_GENDER)))
								.numero(Numero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_NUMBER)))
								.caso(Caso.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_CASE)))
								.clitico(parseYesNoBoolean(getNodeXPathValue(wordForm, xPath, XPATH_CLITIC)))
								.build())
						.filter(Objects::nonNull)
						.filter(flexion -> StringUtils.isNotBlank(flexion.getFlexion()))
						.toList())
                .build();
    }

    private Numeral parseNumeral(Document doc, XPath xPath, String principal) throws XPathException {
        NodeList wordForms = (NodeList) xPath.compile(XPATH_WORDFORMS).evaluate(doc, XPathConstants.NODESET);
        return Numeral.builder()
                .principal(principal)
                .sloleksId(getXPathValue(doc, xPath, XPATH_SLOLEKS_ID))
                .sloleksKey(getXPathValue(doc, xPath, XPATH_SLOLEKS_KEY))
                .listaFlexiones(IntStream.range(0, wordForms.getLength())
						.mapToObj(wordForms::item)
						.map(wordForm -> NumeralFlexion.builder()
								.principal(principal)
								.flexion(getNodeXPathValue(wordForm, xPath, XPATH_FLEXION))
								.acentuado(getNodeXPathValue(wordForm, xPath, XPATH_ACENTUADO))
								.pronunciacionIpa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_IPA))
								.pronunciacionSampa(getNodeXPathValue(wordForm, xPath, XPATH_PRONUNCIACION_SAMPA))
								.genero(Genero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_GENDER)))
								.numero(Numero.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_NUMBER)))
								.caso(Caso.fromCode(getNodeXPathValue(wordForm, xPath, XPATH_CASE)))
								.build())
						.filter(Objects::nonNull)
						.filter(flexion -> StringUtils.isNotBlank(flexion.getFlexion()))
						.toList())
                .build();
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Parsea un string XML a Document
     */
    private Document parseXmlString(String xml) throws SAXException, IOException, ParserConfigurationException {
        return DocumentBuilderFactory.newDefaultInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));
    }

    /**
     * Obtiene un valor XPath desde el documento
     */
    private String getXPathValue(Document doc, XPath xPath, String expression) {
        try {
            return xPath.compile(expression).evaluate(doc);
        } catch (XPathExpressionException e) {
            log.warn("Error evaluando XPath '{}': {}", expression, e.getMessage());
            return "";
        }
    }

    /**
     * Obtiene un valor XPath desde un nodo específico
     */
    private String getNodeXPathValue(Node node, XPath xPath, String expression) {
        try {
            return xPath.compile(expression).evaluate(node);
        } catch (XPathExpressionException e) {
            log.warn("Error evaluando XPath '{}' en nodo: {}", expression, e.getMessage());
            return "";
        }
    }

    /**
     * Convierte un valor XML "yes"/"no" a Boolean
     * @param value el valor del XML
     * @return true si "yes", false si "no", null si está vacío o es otro valor
     */
    private Boolean parseYesNoBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "yes" -> Boolean.TRUE;
            case "no" -> Boolean.FALSE;
            default -> null;
        };
    }

    /**
     * Busca TODAS las entradas con el lema dado en los XMLs
     */
    private List<ResultadoBusqueda> getAllXmlStrings(String word) throws IOException {
        List<ResultadoBusqueda> resultados = new ArrayList<>();
        VTDGen vtdGenerator = new VTDGen();

        Files.walk(Paths.get(xmlPath), 5)
                .filter(p -> p.toFile().isFile())
                .forEach(p -> {
                    log.debug("Buscando en archivo {}", p);
                    try {
                        vtdGenerator.setDoc(Files.readAllBytes(p));
                        vtdGenerator.parse(true);
                        VTDNav vtdNavigator = vtdGenerator.getNav();
                        AutoPilot autoPilot = new AutoPilot(vtdNavigator);
                        autoPilot.selectXPath("/lexicon/entry[head/headword/lemma = '" + word + "']");

                        while (autoPilot.evalXPath() != -1) {
                            long l = vtdNavigator.getContentFragment();
                            String xmlContent = "<entry>" + vtdNavigator.toString((int) l, (int) (l >> 32)) + "</entry>";

                            String tipo = extractCategory(xmlContent);
                            log.debug("Categoría extraída: {}", tipo);
                            resultados.add(ResultadoBusqueda.builder()
                                    .lema(word)
                                    .tipo(tipo)
                                    .tipoEspanol(traducirTipo(tipo))
                                    .soportado(TipoPalabra.fromXmlCode(tipo) != null)
                                    .xmlContent(xmlContent)
                                    .build());
                        }
                    } catch (IOException | VTDException e) {
                        log.warn("Error reading the file {}", p, e);
                    }
                });

        return resultados;
    }

    /**
     * Extrae la categoría de un XML de entrada
     */
    private String extractCategory(String xml) {
        try {
            Document doc = parseXmlString(xml);
            XPath xPath = XPathFactory.newInstance().newXPath();
            return getXPathValue(doc, xPath, "/entry/head/grammar/category/text()");
        } catch (Exception e) {
            log.warn("No se pudo extraer categoría: {}", e.getMessage());
            return "unknown";
        }
    }

    /**
     * Traduce el tipo de palabra del inglés al español
     */
    public static String traducirTipo(String tipo) {
        return switch (tipo) {
            case "noun" -> "Sustantivo";
            case "verb" -> "Verbo";
            case "adjective" -> "Adjetivo";
            case "pronoun" -> "Pronombre";
            case "adverb" -> "Adverbio";
            case "numeral" -> "Numeral";
            case "preposition" -> "Preposición";
            case "conjunction" -> "Conjunción";
            case "particle" -> "Partícula";
            case "interjection" -> "Interjección";
            case "abbreviation" -> "Abreviatura";
            case "residual" -> "Residual";
            default -> tipo;
        };
    }
}

