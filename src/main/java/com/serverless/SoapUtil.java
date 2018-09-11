package com.serverless;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;

public class SoapUtil {

	private static final Logger LOGGER = LogManager.getLogger(SoapUtil.class);

	private static final String DEFAULT_SOAP_PREFIX = "soap";

	private static final String NAMESPACE_PREFIX = "bil";

	private static final String NAMESPACE_URL = "http://www.globe.com/warcraft/wsdl/billing/";

	public static SOAPMessage getSoapMessage() {
		SOAPMessage soapMessage = null;
		try {
			MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			soapMessage = messageFactory.createMessage();
			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
			SOAPHeader soapHeader = soapEnvelope.getHeader();
			SOAPBody soapBody = soapEnvelope.getBody();

			soapEnvelope.setPrefix(DEFAULT_SOAP_PREFIX);
			soapHeader.setPrefix(DEFAULT_SOAP_PREFIX);
			soapBody.setPrefix(DEFAULT_SOAP_PREFIX);

			soapEnvelope.removeNamespaceDeclaration(SOAPConstants.SOAP_ENV_PREFIX);

		} catch (Exception e) {
			LOGGER.error("Error in getSoapMessage method of SoapUtil: ", e);
		}

		return soapMessage;
	}


	public static <T> T parseStringToObject(String msg, Class<T> clazz) {
		T response = null;
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(msg.getBytes());
			SOAPMessage soapMessage = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(null, is);

			JAXBContext jaxbContext = null;
			SOAPBody soapBody = soapMessage.getSOAPBody();

			if (!soapBody.hasFault()) {
				jaxbContext = JAXBContext.newInstance(clazz);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Node node = soapBody.extractContentAsDocument().getDocumentElement();

				JAXBElement<T> jaxbElement = jaxbUnmarshaller.unmarshal(node, clazz);
				response = jaxbElement.getValue();
			}

		} catch (Exception e) {
			LOGGER.error("Error in parseStringToObject method of SoapUtil: ", e);
		}

		return response;
	}

	public static <T> T callSoapWebService(String soapEndpointUrl, SOAPMessage soapMessage, Class<T> clazz) {
		T response = null;

		if (soapMessage == null) {
			LOGGER.info("soapMessage null, error when init soap message");
			return response;
		}

		try {
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			MimeHeaders headers = soapMessage.getMimeHeaders();
			headers.addHeader("SOAPAction", soapEndpointUrl);
			soapMessage.getMimeHeaders().getAllHeaders();
			// Send SOAP Message to SOAP Server
			SOAPMessage soapResponse = soapConnection.call(soapMessage, soapEndpointUrl);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			soapResponse.writeTo(outputStream);
			String messageStr = outputStream.toString(StandardCharsets.UTF_8.displayName());

			SoapUtil.printPretty(soapMessage);
			SoapUtil.printPretty(soapResponse);

			response = SoapUtil.parseStringToObject(messageStr, clazz);

		} catch (Exception e) {
			LOGGER.error("Error in callSoapWebService method of SoapUtil: ", e);
		}
		return response;
	}


	public static void printPretty(SOAPMessage soapMessage) {
		try {
			final TransformerFactory transformerFactory = TransformerFactory.newInstance();
			final Transformer transformer = transformerFactory.newTransformer();

			// Format it
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			final Source soapContent = soapMessage.getSOAPPart().getContent();
			final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
			final StreamResult result = new StreamResult(streamOut);
			transformer.transform(soapContent, result);

			LOGGER.info("\n" + streamOut.toString());

		} catch (Exception e) {
			LOGGER.error("Error in printPretty");
		}
	}

	private SoapUtil() {
		/* Prevent Instantiation */
	}
}
