import { JSEncrypt } from 'jsencrypt'
import { getPubKey } from '@/api/login'

// 设置公钥
const setPublicKey = (encryptor, publicKey) => {
  if (!publicKey) {
    console.error('Public key cannot be null or undefined.')
    return
  }
  encryptor.setPublicKey(publicKey)
}

// 执行加密操作
export async function encrypt (data) {
  try {
    // 获取公钥
    const { result: publicKey } = await getPubKey()

    if (!publicKey) {
      console.error('Public key retrieval failed.')
      return null
    }

    // 创建加密器实例并设置公钥
    const encryptor = new JSEncrypt()
    setPublicKey(encryptor, publicKey)

    if (!encryptor.getPublicKey()) {
      console.error('Public key has not been set.')
      return null
    }

    // 执行加密
    const result = encryptor.encrypt(data)
    if (!result) {
      console.error('Encryption failed. Please check the data and public key.')
      return null
    }
    return result
  } catch (err) {
    console.error('Error in encryption process:', err)
    return null
  }
}
