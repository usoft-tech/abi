package com.usoft.framework.security.utils;

import java.math.BigInteger;
import java.security.Security;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 * SM2 加密工具类
 */
public class Sm2Utils {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * SM2 解密
     * @param privateKeyHex 私钥 (Hex)
     * @param cipherTextHex 密文 (Hex, 04开头C1C3C2格式)
     * @return 明文
     */
    public static String decrypt(String privateKeyHex, String cipherTextHex) {
        try {
            // sm-crypto 产生的密文带 04 前缀，Bouncy Castle SM2Engine C1C3C2 模式需要 04 前缀来解析 C1 点
            byte[] cipherData = Hex.decode(cipherTextHex);
            byte[] privateKeyData = Hex.decode(privateKeyHex);
            
            X9ECParameters sm2ECParameters = GMNamedCurves.getByName("sm2p256v1");
            ECDomainParameters domainParameters = new ECDomainParameters(
                sm2ECParameters.getCurve(), 
                sm2ECParameters.getG(), 
                sm2ECParameters.getN()
            );
            
            ECPrivateKeyParameters privateKeyParameters = new ECPrivateKeyParameters(
                new BigInteger(1, privateKeyData), 
                domainParameters
            );
            
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(false, privateKeyParameters);
            
            byte[] arrayOfByte = engine.processBlock(cipherData, 0, cipherData.length);
            return new String(arrayOfByte, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("SM2 decrypt failed", e);
        }
    }
}
