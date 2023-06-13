package com.skrash.book.data.network

import java.security.MessageDigest

class EncryptIDAlgorithm {

    companion object{

        private fun do32len(idLen16: String): String{
            if (idLen16.length != 16){
                throw java.lang.IllegalArgumentException("length string id is not 16")
            }
            return idLen16 + idLen16
        }

        fun getHexDigestSha1(id: String): String {
            val len32String = do32len(id)
            val sha1 = MessageDigest.getInstance("SHA-256")
            val sha1hash = sha1.digest(len32String.toByteArray(Charsets.UTF_8))
            val builder = StringBuilder()
            for (b in sha1hash) {
                builder.append(String.format("%02x", b))
            }
            return len32String + builder
        }

        fun shuffleAlgorithm(inStr: String): String {
            if (inStr.length != 96) throw RuntimeException("incorrect length")
            var result = ""
            val arrayChunk = inStr.chunked(10)
            for (chunk in arrayChunk) {
                var newChunk = charArrayOf()
                if (chunk.length == 10){
                    newChunk = charArrayOf(
                        chunk[1], chunk[9], chunk[0], chunk[3], chunk[2], chunk[8], chunk[7], chunk[6], chunk[4], chunk[5]
                    )
                }
                if (chunk.length == 6){
                    newChunk = charArrayOf(
                        chunk[1], chunk[0], chunk[3], chunk[2], chunk[4], chunk[5]
                    )
                }
                result += String(newChunk)
            }
            return result
        }
    }
}