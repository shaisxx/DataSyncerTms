/**
 * @description 
 * @author Shen Yan
 * @version 1.0
 * @datetime 2016年12月23日 上午11:11:01
 */
package com.shtd.datasyncer.utils;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;

/**
 * @author Josh
 */
public class EncryptionUtil {
    static PooledPBEStringEncryptor encryptor = null;
    static {
        encryptor = new PooledPBEStringEncryptor();
        encryptor.setPoolSize(4); 
        encryptor.setPassword("s4h8t3d6");
        encryptor.setAlgorithm("PBEWITHMD5ANDDES");
    }

    public static String encrypt(String input) {
        return encryptor.encrypt(input);
    }

    public static String decrypt(String encryptedMessage) {
        return encryptor.decrypt(encryptedMessage);
    }
    
    public static void main(String[] args){
    	PooledPBEStringEncryptor pooledPBEStringEncryptor = new PooledPBEStringEncryptor();
    	pooledPBEStringEncryptor.setPoolSize(4); 
    	pooledPBEStringEncryptor.setPassword("s4h8t3d6");
    	pooledPBEStringEncryptor.setAlgorithm("PBEWITHMD5ANDDES");
        String encrypted = pooledPBEStringEncryptor.encrypt("123456");  
        System.out.println(encrypted);
        
        String decrypted = pooledPBEStringEncryptor.decrypt("nH4LWbdzhKiW2YLBiucm/g==");
        System.out.println(decrypted);
    }
}
