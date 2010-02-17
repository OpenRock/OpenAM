/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://opensso.dev.java.net/public/CDDLv1.0.html or
 * opensso/legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at opensso/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * $Id: Saml2Utils.cs,v 1.5 2009/11/11 18:13:39 ggennaro Exp $
 */

using System;
using System.Collections.Specialized;
using System.Globalization;
using System.IO;
using System.IO.Compression;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Security.Cryptography.Xml;
using System.Text;
using System.Web;
using System.Xml;
using System.Xml.XPath;
using Sun.Identity.Common;
using Sun.Identity.Properties;
using Sun.Identity.Saml2.Exceptions;

namespace Sun.Identity.Saml2
{
    /// <summary>
    /// Utility class for performing SAMLv2 operations.
    /// </summary>
    public static class Saml2Utils
    {
        #region Methods

        /// <summary>
        /// Converts the string from the base64 encoded input.
        /// </summary>
        /// <param name="value">Base64 encoded string.</param>
        /// <returns>String contained within the base64 encoded string.</returns>
        public static string ConvertFromBase64(string value)
        {
            byte[] byteArray = Convert.FromBase64String(value);
            return Encoding.UTF8.GetString(byteArray);
        }

        /// <summary>
        /// Converts from Base64, then decompresses the given
        /// parameter and returns the ensuing string.
        /// </summary>
        /// <param name="message">message to undergo the process</param>
        /// <returns>String output from the process.</returns>
        public static string ConvertFromBase64Decompress(string message)
        {
            // convert from base 64
            byte[] byteArray = Convert.FromBase64String(message);

            // inflate the gzip deflated message
            StreamReader streamReader = new StreamReader(new DeflateStream(new MemoryStream(byteArray), CompressionMode.Decompress));

            // put in a string
            string decompressedMessage = streamReader.ReadToEnd();
            streamReader.Close();

            return decompressedMessage;
        }

        /// <summary>
        /// Converts the base64 encoded string of the given input string.
        /// </summary>
        /// <param name="value">String to be encoded.</param>
        /// <returns>Base64 encoded output of the specified string.</returns>
        public static string ConvertToBase64(string value)
        {
            return Convert.ToBase64String(Encoding.UTF8.GetBytes(value));
        }

        /// <summary>
        /// Creates a SOAP message, with no header, to encompass the specified
        /// xml payload in its body.
        /// </summary>
        /// <param name="xmlPayload">XML to be placed within the body of this message.</param>
        /// <returns>String representation of the SOAP message.</returns>
        public static string CreateSoapMessage(string xmlPayload)
        {
            StringBuilder soapMessage = new StringBuilder();
            soapMessage.Append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            soapMessage.Append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" >");
            soapMessage.Append("  <soap:Body>");
            soapMessage.Append(xmlPayload);
            soapMessage.Append("  </soap:Body>");
            soapMessage.Append("</soap:Envelope>");

            return soapMessage.ToString();
        }

        /// <summary>
        /// Generates a random ID for use in SAMLv2 assertions, requests, and
        /// responses.
        /// </summary>
        /// <returns>String representing a random ID with length specified by Saml2Constants.IdLength</returns>
        public static string GenerateId()
        {
            Random random = new Random();
            byte[] byteArray = new byte[Saml2Constants.IdLength];
            random.NextBytes(byteArray);
            string id = BitConverter.ToString(byteArray).Replace("-", string.Empty);

            return id;
        }

        /// <summary>
        /// Generates the current time, in UTC, formatted in the invariant
        /// culture format for use in SAMLv2 assertions, requests, and
        /// responses.
        /// </summary>
        /// <returns>Current time in UTC, invariant culture format.</returns>
        public static string GenerateIssueInstant()
        {
            string issueInstant = DateTime.UtcNow.ToString("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'", DateTimeFormatInfo.InvariantInfo);

            return issueInstant;
        }

        /// <summary>
        /// Gets the request parameters and returns them within a NameValueCollection.
        /// </summary>
        /// <param name="request">HttpRequest containing desired parameters</param>
        /// <returns>
        /// NameValueCOllection of parameters found in QueryString and Form objects within 
        /// the given Request.
        /// </returns>
        public static NameValueCollection GetRequestParameters(HttpRequest request)
        {
            NameValueCollection parameters = new NameValueCollection();

            foreach (string name in request.QueryString.Keys)
            {
                parameters[name] = request.QueryString[name];
            }

            foreach (string name in request.Form.Keys)
            {
                parameters[name] = request.Form[name];
            }

            return parameters;
        }

        /// <summary>
        /// Gets the boolean value from the string using Boolean.Parse(string)
        /// but handles exception.
        /// </summary>
        /// <param name="value">String to parse.</param>
        /// <returns>
        /// Results from Boolean.Parse(string), false if exception thrown.
        /// </returns>
        public static bool GetBoolean(string value)
        {
            try
            {
                return Boolean.Parse(value);
            }
            catch (ArgumentNullException)
            {
                return false;
            }
            catch (FormatException)
            {
                return false;
            }
        }

        /// <summary>
        /// Compresses, converts to Base64, then URL encodes the given 
        /// parameter and returns the ensuing string.
        /// </summary>
        /// <param name="xml">XML to undergo the process</param>
        /// <returns>String output from the process.</returns>
        public static string CompressConvertToBase64UrlEncode(IXPathNavigable xml)
        {
            XmlDocument xmlDoc = (XmlDocument)xml;

            byte[] buffer = Encoding.UTF8.GetBytes(xmlDoc.OuterXml);
            MemoryStream memoryStream = new MemoryStream();
            DeflateStream compressedStream = new DeflateStream(memoryStream, CompressionMode.Compress, true);
            compressedStream.Write(buffer, 0, buffer.Length);
            compressedStream.Close();

            memoryStream.Position = 0;
            byte[] compressedBuffer = new byte[memoryStream.Length];
            memoryStream.Read(compressedBuffer, 0, compressedBuffer.Length);
            memoryStream.Close();

            string compressedBase64String = Convert.ToBase64String(compressedBuffer);
            string compressedBase64UrlEncodedString = HttpUtility.UrlEncode(compressedBase64String);

            return compressedBase64UrlEncodedString;
        }

        /// <summary>
        /// URL decodes, converts from Base64, then decompresses the given
        /// parameter and returns the ensuing string.
        /// </summary>
        /// <param name="message">message to undergo the process</param>
        /// <returns>String output from the process.</returns>
        public static string UrlDecodeConvertFromBase64Decompress(string message)
        {
            // url decode it
            string decodedMessage = HttpUtility.UrlDecode(message);

            // convert from base 64
            byte[] byteArray = Convert.FromBase64String(decodedMessage);

            // inflate the gzip deflated message
            StreamReader streamReader = new StreamReader(new DeflateStream(new MemoryStream(byteArray), CompressionMode.Decompress));

            // put in a string
            string decompressedMessage = streamReader.ReadToEnd();
            streamReader.Close();

            return decompressedMessage;
        }

        /// <summary>
        /// Validates a signed xml document with the given certificate,
        /// the xml signature, and the target reference id.
        /// </summary>
        /// <param name="cert">
        /// X509Certificate used to verify the signature of the xml document.
        /// </param>
        /// <param name="xmlDoc">
        /// XML document whose signature will be checked.
        /// </param>
        /// <param name="xmlSignature">Signature of the XML document.</param>
        /// <param name="targetReferenceId">
        /// Reference element that should be signed.
        /// </param>
        public static void ValidateSignedXml(X509Certificate2 cert, IXPathNavigable xmlDoc, IXPathNavigable xmlSignature, string targetReferenceId)
        {
            SignedXml signedXml = new SignedXml((XmlDocument)xmlDoc);
            signedXml.LoadXml((XmlElement)xmlSignature);

            bool results = signedXml.CheckSignature(cert, true);
            if (results == false)
            {
                throw new Saml2Exception(Resources.SignedXmlCheckSignatureFailed);
            }

            bool foundValidSignedReference = false;
            foreach (Reference r in signedXml.SignedInfo.References)
            {
                string referenceId = r.Uri.Substring(1);
                if (referenceId == targetReferenceId)
                {
                    foundValidSignedReference = true;
                }
            }

            if (!foundValidSignedReference)
            {
                throw new Saml2Exception(Resources.SignedXmlInvalidReference);
            }
        }

        /// <summary>
        /// Validates a signed query string.
        /// </summary>
        /// <param name="cert">
        /// X509Certificate used to verify the signature of the xml document.
        /// </param>
        /// <param name="queryString">
        /// Query string to validate.  SigAlg and Signature are expected
        /// to in the set of parameters.
        /// </param>
        public static void ValidateSignedQueryString(X509Certificate2 cert, string queryString)
        {
            if (cert == null)
            {
                throw new Saml2Exception(Resources.SignedQueryStringCertIsNull);
            }

            if (string.IsNullOrEmpty(queryString))
            {
                throw new Saml2Exception(Resources.SignedQueryStringIsNull);
            }

            char[] queryStringSep = { '&' };
            NameValueCollection queryParams = new NameValueCollection();
            foreach (string pairs in queryString.Split(queryStringSep))
            {
                string key = pairs.Substring(0, pairs.IndexOf("=", StringComparison.Ordinal));
                string value = pairs.Substring(pairs.IndexOf("=", StringComparison.Ordinal) + 1);

                queryParams[key] = value;
            }

            if (string.IsNullOrEmpty(queryParams[Saml2Constants.SignatureAlgorithm]))
            {
                throw new Saml2Exception(Resources.SignedQueryStringMissingSigAlg);
            }

            if (string.IsNullOrEmpty(queryParams[Saml2Constants.Signature]))
            {
                throw new Saml2Exception(Resources.SignedQueryStringMissingSignature);
            }

            string sigAlg = HttpUtility.UrlDecode(queryParams[Saml2Constants.SignatureAlgorithm]);
            string signature = HttpUtility.UrlDecode(queryParams[Saml2Constants.Signature]);
            
            // construct a new query string with specific sequence and no signature param
            string newQueryString = string.Empty;
            if (!string.IsNullOrEmpty(queryParams[Saml2Constants.RequestParameter]))
            {
                newQueryString = Saml2Constants.RequestParameter + "=" + queryParams[Saml2Constants.RequestParameter];
            }
            else if (!string.IsNullOrEmpty(queryParams[Saml2Constants.ResponseParameter]))
            {
                newQueryString = Saml2Constants.ResponseParameter + "=" + queryParams[Saml2Constants.ResponseParameter];
            }

            if (!string.IsNullOrEmpty(queryParams[Saml2Constants.RelayState]))
            {
                newQueryString += "&" + Saml2Constants.RelayState + "=" + queryParams[Saml2Constants.RelayState];
            }

            newQueryString += "&" + Saml2Constants.SignatureAlgorithm + "=" + queryParams[Saml2Constants.SignatureAlgorithm];

            byte[] dataBuffer = Encoding.UTF8.GetBytes(newQueryString);
            byte[] sigBuffer = Convert.FromBase64String(signature);

            if (sigAlg == Saml2Constants.SignatureAlgorithmDsa)
            {
                /*
                 * Issues with the way the signature is created in 
                 * Java (DER Encoding) versus what is used in the 
                 * .NET framework (IEEE P1363 standard).
                 * 
                 * TODO: Will need to create the DSA signature converter
                 * DSACryptoServiceProvider publicKey = (DSACryptoServiceProvider)cert.PublicKey.Key;
                 * if(!publicKey.VerifyData(dataBuffer, sigBuffer)) {
                 *      throw new Saml2Exception(Resources.SignedQueryStringVerifyDataFailed);
                 * }
                 */
                throw new Saml2Exception(Resources.SignedQueryStringUnsupportedSigAlg);
            }
            else if (sigAlg == Saml2Constants.SignatureAlgorithmRsa)
            {
                RSACryptoServiceProvider publicKey = (RSACryptoServiceProvider)cert.PublicKey.Key;
                if (!publicKey.VerifyData(dataBuffer, new SHA1CryptoServiceProvider(), sigBuffer))
                {
                    throw new Saml2Exception(Resources.SignedQueryStringVerifyDataFailed);
                }
            }
            else
            {
                throw new Saml2Exception(Resources.SignedQueryStringUnsupportedSigAlg);
            }
        }

        #endregion
    }
}
