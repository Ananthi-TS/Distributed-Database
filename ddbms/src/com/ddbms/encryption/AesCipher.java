package com.ddbms.encryption;

import java.security.SecureRandom;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class AesCipher {

	private static final String algorithm = "AES/CTR/NoPadding";
	
	static String keyValue=Hex.encodeHexString("group-20".getBytes());
	
	public static String encrypt(String valueToBeEncryptd)  {
		String encryptedValue = "";
		
		try {
			SecretKeySpec keySpec = extractKey(keyValue);
			SecureRandom random = new SecureRandom();
			byte[] ivValue = new byte[16];	        
			random.nextBytes(ivValue); 
			IvParameterSpec ivSpec = new IvParameterSpec(ivValue);
			Cipher objCipher = Cipher.getInstance(algorithm);
			objCipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			byte[] encryptedTextBytes = objCipher.doFinal(valueToBeEncryptd.getBytes());
			String encodedText = Base64.getEncoder().encodeToString(encryptedTextBytes);
			String encodedIvText = Hex.encodeHexString(ivValue);
			encryptedValue = encodedIvText + "::" + encodedText;
		} catch (Exception e) {
			e.printStackTrace();
		}	 
		
		
		return encryptedValue;
		
	}

	private static SecretKeySpec extractKey(String keyValue) throws DecoderException {
		String decryptedKey= new String(Hex.decodeHex(keyValue.toCharArray()));
		SecretKeySpec keySpec = new SecretKeySpec(padString(decryptedKey).getBytes(), "AES");
		return keySpec;
	}
	
	public static String decrypt(String encryptedDataInput)  {
		
		String decryptedValue = "";
		
    	try {
			String strIvVal = "";
			String strEncryptedVal = "";
			SecretKeySpec keySpec = extractKey(keyValue);
			if ((encryptedDataInput == null) || (encryptedDataInput.split("::").length < 2)) {
				
				throw new IllegalArgumentException("Invalid data");
			}
			strIvVal = encryptedDataInput.split("::")[0];
			strEncryptedVal = encryptedDataInput.split("::")[1];	    		
			byte[] ivValue = Hex.decodeHex(strIvVal.toCharArray());	    		
			byte[] encryptedTextBytes = Base64.getDecoder().decode(strEncryptedVal);
		    IvParameterSpec ivSpec = new IvParameterSpec(ivValue);
		    Cipher objCipher = Cipher.getInstance(algorithm);
		    objCipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);	 
		    byte[] decValueBytes = objCipher.doFinal(encryptedTextBytes);
		    decryptedValue = new String(decValueBytes);	
		} catch (Exception e) {
		} 
		
		
		return decryptedValue;
	}
	
	
	private static String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;
        for (int i = 0; i < padLength; i++)
        {
            source += paddingChar;
        }
        return source;
      }
	
	
	
}
