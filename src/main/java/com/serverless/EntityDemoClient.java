package com.serverless;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import com.xignite.services.GetAllCrossRatesForACurrency;
import com.xignite.services.GetAllCrossRatesForACurrencyResponse;

public class EntityDemoClient {

	private static final Logger LOGGER = LogManager.getLogger(EntityDemoClient.class);

	public static void main(String[] args) {

		try {

			GetAllCrossRatesForACurrency getAllCrossRatesForACurrency = new GetAllCrossRatesForACurrency();
			getAllCrossRatesForACurrency.setSymbol("USD");

			EntityDemoClient entityDemoClient = new EntityDemoClient();

			GetAllCrossRatesForACurrencyResponse getAllCrossRatesForACurrencyResponse = entityDemoClient.getPersion(getAllCrossRatesForACurrency);
			if (getAllCrossRatesForACurrencyResponse != null) {
				LOGGER.info(getAllCrossRatesForACurrencyResponse.getGetAllCrossRatesForACurrencyResult().getOutcome());
			}

//			Gson gson = new GsonBuilder().setPrettyPrinting().create();
//			LOGGER.info("\n" + gson.toJson(response));

		} catch (Exception e) {
			LOGGER.error("Error in get subscribe");
		}

	}

	public GetAllCrossRatesForACurrencyResponse getPersion(GetAllCrossRatesForACurrency request) {
		GetAllCrossRatesForACurrencyResponse response = null;
		String endpoint = "http://www.xignite.com/xcurrencies.asmx";
		LOGGER.info("demo find persion request to endpoint: " + endpoint);
		SOAPMessage soapMessage = updateCurrencySoapMessage(request);

		response = SoapUtil.callSoapWebService(endpoint, soapMessage, GetAllCrossRatesForACurrencyResponse.class);

		return response;
	}

	public static SOAPMessage updateCurrencySoapMessage(GetAllCrossRatesForACurrency request) {
		SOAPMessage soapMessage = null;
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			JAXBContext context = JAXBContext.newInstance(GetAllCrossRatesForACurrency.class);
			Marshaller marshaller = context.createMarshaller();

			MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
			//for pretty-print XML in JAXB
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(request, document);
			soapMessage = messageFactory.createMessage();

			SOAPPart soapPart = soapMessage.getSOAPPart();
			SOAPEnvelope envelope = soapPart.getEnvelope();
			SOAPHeader header = soapMessage.getSOAPHeader();
			SOAPBody body = soapMessage.getSOAPBody();

			envelope.setPrefix(EsbConstants.SOAPENV);
			header.setPrefix(EsbConstants.SOAPENV);
			body.setPrefix(EsbConstants.SOAPENV);

			envelope.removeNamespaceDeclaration(SOAPConstants.SOAP_ENV_PREFIX);
			envelope.addNamespaceDeclaration("ser", "http://www.xignite.com/services/");

			body.addDocument(document);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return soapMessage;
	}


}
