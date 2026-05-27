import CryptoJS from 'crypto-js';
import { sm2, sm3 } from 'sm-crypto';

/**
 * 加密工具类
 * 封装 SM2, SM3, MD5, SHA256, Base64 等常用加密方法
 */
export class CryptoUtils {

  /**
   * SM2 公钥加密
   * @param word 待加密字符串
   * @param publicKey 公钥 (Hex 字符串)
   * @returns 加密后的 Hex 字符串 (04 + C1C3C2)
   */
  public static encryptSM2(word: string, publicKey: string): string {
    // 1 表示 C1C3C2 模式
    let cipher = sm2.doEncrypt(word, publicKey, 1);
    // sm-crypto 默认不带 04 前缀，需手动补全
    if (cipher && !cipher.startsWith('04')) {
      cipher = '04' + cipher;
    }
    return cipher;
  }

  /**
   * SM3 摘要/哈希
   * @param word 待计算字符串
   * @returns 哈希值 (Hex 字符串)
   */
  public static hashSM3(word: string): string {
    return sm3(word);
  }

  /**
   * MD5 加密
   * @param word 待加密字符串
   * @returns 32位小写密文
   */
  public static hashMD5(word: string): string {
    return CryptoJS.MD5(word).toString();
  }

  /**
   * SHA256 加密
   * @param word 待加密字符串
   * @returns 密文
   */
  public static hashSHA256(word: string): string {
    return CryptoJS.SHA256(word).toString();
  }

  /**
   * Base64 编码
   * @param word 原字符串
   * @returns Base64字符串
   */
  public static encodeBase64(word: string): string {
    const srcs = CryptoJS.enc.Utf8.parse(word);
    return CryptoJS.enc.Base64.stringify(srcs);
  }

  /**
   * Base64 解码
   * @param word Base64字符串
   * @returns 原字符串
   */
  public static decodeBase64(word: string): string {
    const srcs = CryptoJS.enc.Base64.parse(word);
    return CryptoJS.enc.Utf8.stringify(srcs);
  }
}
