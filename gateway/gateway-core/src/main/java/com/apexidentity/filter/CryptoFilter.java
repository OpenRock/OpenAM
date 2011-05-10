/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 */

package com.apexidentity.filter;

// Java Standard Edition
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

// ApexIdentity Core Library
import com.apexidentity.handler.HandlerException;
import com.apexidentity.heap.HeapException;
import com.apexidentity.heap.NestedHeaplet;
import com.apexidentity.http.Exchange;
import com.apexidentity.http.Headers;
import com.apexidentity.http.Message;
import com.apexidentity.http.MessageType;
import com.apexidentity.log.LogTimer;
import com.apexidentity.model.MapNode;
import com.apexidentity.model.ModelException;
import com.apexidentity.util.CaseInsensitiveSet;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

/**
 * Encrypts and Decrypts headers
 *
 */
public class CryptoFilter extends GenericFilter {

    /** Indicates the type of message in the exchange to process headers for. */
    MessageType messageType;

    /** ENCRYPT or DECRYPT*/
    String operationType;

    /** Crypto algorithm defaults to DES/ECB/NoPadding */
    String algorithm;

    /** key for encryption, should be base 64 encoded*/
    String key;

    /** key type for key generation, defaults to DES*/
    String keyType;

    /** key type for key generation, defaults to DES*/
    String charSet;

    /** The names of the headers whose values should be processed for encryption or decryption */
    public final CaseInsensitiveSet cryptoHeaders = new CaseInsensitiveSet();

    /**
     * Finds all headers marked for processing and either encrypts or decrypts the values
     *
     * @param message the message to encrypt and decrypt to.
     */
    private void process(Message message) {

        for (String s : this.cryptoHeaders) {
            List<String> inValues = message.headers.get(s);
            if (inValues == null) {
                   continue;
            }
            List<String> outValues = new ArrayList<String>();
            message.headers.remove(s);
            for (String val : inValues) {
               if (operationType.equals("ENCRYPT")) {
                   outValues.add(encrypt(val));
               }
               else {
                   outValues.add(decrypt(val));
               }
            }
            message.headers.addAll(s, outValues);
        }
    }
 
    /**
     * Returns the decrypted string
     *
     * @param in the string to decrypt
     */
    private String decrypt(String in) {

        String decryptedVal = null;
        try {
          BASE64Decoder decoder = new BASE64Decoder();
          byte[] desKey = decoder.decodeBuffer(this.key);
          byte[] encIn = decoder.decodeBuffer(in);
          SecretKeySpec keySpec = new SecretKeySpec(desKey, this.keyType);
          Cipher cipher = Cipher.getInstance(this.algorithm);
          cipher.init(Cipher.DECRYPT_MODE, keySpec);
          byte[] plainText = cipher.doFinal(encIn);
          decryptedVal =  new String(plainText, charSet).trim();

        } catch (IOException ioe) {
            System.out.println("CryptoFilter.decrypt:" + ioe);
        } catch (IllegalBlockSizeException ibe) {
            System.out.println("CryptoFilter.decrypt:" + ibe);
        } catch (NoSuchPaddingException npe) {
            System.out.println("CryptoFilter.decrypt:" + npe);
        } catch (NoSuchAlgorithmException noe) {
            System.out.println("CryptoFilter.decrypt:" + noe);
        } catch (InvalidKeyException ike) {
            System.out.println("CryptoFilter.decrypt:" + ike);
        } catch (BadPaddingException bpe) {
            System.out.println("CryptoFilter.decrypt:" + bpe);
        }
        return decryptedVal;
    }

    /**
     * Returns the encrypted string
     *
     * @param in the string to encrypt
     */
    private String encrypt(String in) {

        String encryptedVal = null;
        try {
          BASE64Decoder decoder = new BASE64Decoder();
          byte[] desKey = decoder.decodeBuffer(this.key);
          SecretKeySpec keySpec = new SecretKeySpec(desKey, this.keyType);
          Cipher cipher = Cipher.getInstance(this.algorithm);
          cipher.init(Cipher.ENCRYPT_MODE, keySpec);
          byte[] ciphertext = cipher.doFinal(in.getBytes());
          BASE64Encoder encoder = new BASE64Encoder();
          encryptedVal = encoder.encodeBuffer(ciphertext).trim();

        } catch (IOException ioe) {
            System.out.println("CryptoFilter.encrypt:" + ioe);
        } catch (IllegalBlockSizeException ibe) {
            System.out.println("CryptoFilter.encrypt:" + ibe);
        } catch (NoSuchPaddingException npe) {
            System.out.println("CryptoFilter.encrypt:" + npe);
        } catch (NoSuchAlgorithmException noe) {
            System.out.println("CryptoFilter.encrypt:" + noe);
        } catch (InvalidKeyException ike) {
            System.out.println("CryptoFilter.encrypt:" + ike);
        } catch (BadPaddingException bpe) {
            System.out.println("CryptoFilter.encrypt:" + bpe);
        }
        return encryptedVal;
    }

    /**
     * Filters the request and/or response of an exchange by removing headers from and adding
     * headers to a message.
     */
    @Override
    public void filter(Exchange exchange, Chain chain) throws HandlerException, IOException {
        LogTimer timer = logger.getTimer().start();
        if (messageType == MessageType.REQUEST) {
            process(exchange.request);
        }
        chain.handle(exchange);
        if (messageType == MessageType.RESPONSE) {
            process(exchange.response);
        }
        timer.stop();
    }

    /** Creates and initializes a header filter in a heap environment. */
    public static class Heaplet extends NestedHeaplet {
        @Override public Object create() throws HeapException, ModelException {
            CryptoFilter filter = new CryptoFilter();
            filter.messageType = config.get("messageType").required().asEnum(MessageType.class);
            filter.operationType = config.get("operationType").required().asString();
            filter.algorithm = config.get("algorithm").defaultTo("DES/ECB/NoPadding").asString();
            filter.key = config.get("key").required().asString();
            filter.charSet = config.get("charSet").defaultTo("utf-8").asString();
            filter.keyType = config.get("keyType").defaultTo("DES").asString();
            filter.cryptoHeaders.addAll(config.get("headers").defaultTo(Collections.emptyList()).asList(String.class));
            return filter;
        }
    }
}
