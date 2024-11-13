package com.parallax.video.wallpaper

import android.util.Log
import com.google.gson.internal.Primitives
import com.parallax.video.wallpaper.extension.round
import com.parallax.video.wallpaper.extension.toHex
import com.parallax.video.wallpaper.utils.Constants
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.junit.Assert
import org.junit.Test
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.reflect.KClass


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    val listClass = mutableListOf<KClass<*>>()
    @Test
    fun addition_isCorrect() {
        val ss = "abc \n"
        println(ss.trim())
        var list0 = mutableListOf<Int>()
        var list = mutableListOf(1, 2, 3, 4, 5,6)

        list0 = list
        val time = System.nanoTime()
        list.add(8)
        println(list0)
        println(System.nanoTime() - time)
        listClass.add(Int::class)
        val any: Any = 1
        val xx = cast(listClass[0], any)
        Assert.assertEquals(xx != null, true)
//        assertEquals(4, 2 + 2)//1595388640650//6500920364400//1595388731068
    }

    private fun getString(vararg objects: Any?) : String {
        return objects.foldIndexed(""){index, acc, any ->
            if (index > 0) acc.plus(" ; ").plus(any)
            else any.toString()
        }
    }

    @Test
    fun find() {
//        val string = "onJsLoad();</script><title>Shared album - ĐẠT TRẦN - Google Photos</title><meta name=\"robots\" content=\"noindex\"><meta property=\"og:title\" content=\"New video by ĐẠT TRẦN\"><meta property=\"og:type\" content=\"video\"><meta property=\"og:url\" content=\"https://photos.google.com/share/AF1QipP-TlLqqBaaqLLlyI4ZumoQrZPOkdUGEbzgJBLHUhC7JbGvmMb4frzLH8Ib3Z2kQg?key=QjJjODhkd21jb2swRkFYZ2lMQjdsWkxOVWxZZWVn\"><meta property=\"og:image\" content=\"https://lh3.googleusercontent.com/NSGaIp13tCnZq2Rlj0wVZAad4eBLqM_wWBWxfO3l8d8FeFYhRNvGdShdX_J5xwR2GQsoAWiU9NRKt9pHsN9KhuQG9Ejvx-sIQfhyaYY0boM2bm6mWTespEanK3p9CV_FUsCMoBJiBg=w600-h315-p-k-no\"><meta property=\"og:image:width\" content=\"600\"><meta property=\"og:image:height\" content=\"315\"><meta name=\"twitter:card\" content=\"photo\"><meta name=\"twitter:site\" content=\"@googlephotos\"><meta name=\"fb:app_id\" content=\"1408502372789355\"><meta property=\"og:video\" content=\"https://lh3.googleusercontent.com/NSGaIp13tCnZq2Rlj0wVZAad4eBLqM_wWBWxfO3l8d8FeFYhRNvGdShdX_J5xwR2GQsoAWiU9NRKt9pHsN9KhuQG9Ejvx-sIQfhyaYY0boM2bm6mWTespEanK3p9CV_FUsCMoBJiBg=w600-h315-k-no-m18\"><meta property=\"og:video:secure_url\" content=\"https://lh3.googleusercontent.com/NSGaIp13tCnZq2Rlj0wVZAad4eBLqM_wWBWxfO3l8d8FeFYhRNvGdShdX_J5xwR2GQsoAWiU9NRKt9pHsN9KhuQG9Ejvx-sIQfhyaYY0boM2bm6mWTespEanK3p9CV_FUsCMoBJiBg=w600-h315-k-no-m18\"><meta property=\"og:video:type\" content=\"video/mp4\"><meta property=\"og:video:width\" content=\"600\"><meta property=\"og:video:height\" content=\"315\"><script id='og-inHeadScript' nonce=\"ey8e/fxj7OCJPrK/+mpK8g\">;this.gbar_={CONFIG:[[[0,\"www.gstatic.com\",\"og.qtm.en_US.EqBcnH-TWRk.O\",\"com.vn\",\"en\",\"31\",0,[4,2,\".40.40.40.40.40.40.\",\"\",\"1300102,3700303,3700697\",\"307318782\",\"0\"],null,\"liepXsrAOYXY-wTO7LyYBQ\",null,0,\"og.qtm.15aandm11is4m.L.X.O\",\"AA2YrTv1lGDphP_8g_rszPCsqy8KYgA11Q\",\"AA2YrTtT-VVtOi-uymSLz2WjDFUwP0j3-Q\",\"\",2,1,200,\"VNM\",null,null,\"269\",\"31\",1],null,[1,0.1000000014901161,2,1],[1,0.001000000047497451,1],[0,0,0,null,\"\",\"\",\"\",\"\"],[0,0,\"\",1,0,0,0,0,0,0,0,0,0,null,0,0,null,null,0,0,0,\"\",\"\",\"\",\"\",\"\",\"\",null,0,0,0,0,0,null,null,null,\"rgba(32,33,36,1)\",\"transparent\",0,0,1,1],null,null,[\"1\",\"gci_91f30755d6a6b787dcc2a4062e6e9824.js\",\"googleapis.client:plusone:gapi.iframes\",\"\",\"en\"],null,null,[0,\"\",null,\"\",0,\"There was an error loading your Marketplace apps.\",\"You have no Marketplace apps.\",0,[31,\"https://photos.google.com/?tab=qq\\u0026pageId=none\",\"Photos\",\"_blank\",\"0 -621px\",null,0],null,null,null,0,null,null,0],null,[\"m;/_/scs/abc-static/_/js/k=gapi.gapi.en.jw7XZHvcak8.O/d=1/ct=zgms/rs=AHpOoo-L1iz4xVj0PCdm2On38RCj6aYemA/m=__features__\",\"https://apis.google.com\",\"\",\"\",\"\",\"\",null,1,\"es_plusone_gc_20200310.0_p0\",\"en\",null,0,0],[0.009999999776482582,\"com.vn\",\"31\",[null,\"\",\"0\",null,1,5184000,null,null,\"\",0,1,\"\",0,0,0,0,0,0,1,0,0,0],null,[[\"\",\"\",\"0\",0,0,-1]],null,0,null,null,[\"5061451\",\"google\\\\.(com|ru|ca|by|kz|com\\\\.mx|com\\\\.tr)\$\",1]],[1,1,0,27043,31,\"VNM\",\"en\",\"307318782.0\",8,0.009999999776482582,0,0,null,null,0,0,\"\",null,null,null,\"liepXsrAOYXY-wTO7LyYBQ\",0],[[null,null,null,\"https://www.gstatic.com/og/_/js/k=og.qtm.en_US.EqBcnH-TWRk.O/rt=j/m=qabr,q_d,qawd,qsd,qmutsd,qapid/exm=qaaw,qadd,qaid,qein,qhaw,qhbr,qhch,qhga,qhid,qhin,qhpr/d=1/ed=1/rs=AA2YrTv1lGDphP_8g_rszPCsqy8KYgA11Q\"],[null,null,null,\"https://www.gstatic.com/og/_/ss/k=og.qtm.15aandm11is4m.L.X.O/m=qawd/excm=qaaw,qadd,qaid,qein,qhaw,qhbr,qhch,qhga,qhid,qhin,qhpr/d=1/ed=1/ct=zgms/rs=AA2YrTtT-VVtOi-uymSLz2WjDFUwP0j3-Q\"]],null,null,[\"\"]]],};this.gbar_=this.gbar_||{};(function(_){var window=this;"
//        val match = Pattern.compile("og:video.+?content=\"(.+?)\">").matcher(string)
//        if (match.find()) {
//            val str = match.splash_screen_default_background(1)?.split("=")?.firstOrNull() ?: return
//            println(str)
//        }
        val testStr = "Google Photos xin chao cac ban"
//        println(Hex.bytesToStringLowercase(testStr.toByteArray()))
//        println(Aes.enc(testStr, ""))
//        println(Aes.encrypt(testStr, "a"))
        println("config".toHex("1"))
        println(1597057465753.0.round(2))

        println(getString(1, 3, 4))
        (0 until  0).forEach { println("abcd") }
//        println(AES.encrypt(testStr, key))
//        println("hashCode = "  + String.format("%x", BigInteger(1, testStr.toByteArray())))
    }

    fun <T: Any> cast(classOfT: KClass<T>, data: Any): T? {
        return Primitives.wrap(classOfT.java).cast(data)
    }
    private val zzgy = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
    private val zzgz = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    fun toHex(str: String, pass: String) : String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(pass.toByteArray())
        val bytes = md.digest(str.toByteArray())
        val sb = StringBuilder()
        for (element in bytes) {
            sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        println("test = " + Base64.getEncoder().encodeToString(bytes))
        return sb.toString()
    }

    private fun getSalts(): ByteArray {
        //Always use a SecureRandom generator
        val sr = SecureRandom.getInstance("SHA1PRNG", "SUN")
        //Create array for salt
        val salt = ByteArray(16)
        //Get a random salt
        sr.nextBytes(salt)
        //return salt
        return salt
    }


    fun toHexString(str: String): String {
        val array = str.toByteArray()
        val var1 = CharArray(array.size shl 1)
        var var2 = 0

        array.forEach {
            val var4: Int = (it and 255.toByte()).toInt()
            var1[var2++] = zzgz[var4 ushr 4]
            var1[var2++] = zzgz[var4 and 15]
        }
        return String(var1)
    }


    fun getMd5Hash(input: String): String? {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val number = BigInteger(1, messageDigest)
            var md5 = number.toString(16)
            while (md5.length < 32) md5 = "0$md5"
            md5
        } catch (e: NoSuchAlgorithmException) {
            Log.e("MD5", e.localizedMessage)
            null
        }
    }

    private val key = "12345678"
    private val initVector = "encryptionIntVec"

    fun encrypt(value: String): String? {
        try {

            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            return Base64.getEncoder().encodeToString(encrypted)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun getSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(20)
        random.nextBytes(bytes)
        return String(bytes)
    }

    private val secretKey = "boooooooooom!!!!"
    private val salt = "ssshhhhhhhhhhh!!!!"

    fun encrypt256(strToEncrypt: String): String? {
        try {
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivspec = IvParameterSpec(iv)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(key.toCharArray(), initVector.toByteArray(), 65536, 256)
            val tmp: SecretKey = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec)
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))))
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt: String?): String? {
        try {
            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
            val ivspec = IvParameterSpec(iv)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(secretKey.toCharArray(), salt.toByteArray(), 65536, 256)
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec)
            return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
        } catch (e: java.lang.Exception) {
            println("Error while decrypting: $e")
        }
        return null
    }

    class Aes {

        private constructor()

        private var salt = "abc"
        private var secretKey = "12345678"




//        init {
//            val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
//            val ivspec = IvParameterSpec(iv)
//            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//            salt = getSalt()
//            val spec: KeySpec = PBEKeySpec(secretKey.toCharArray(), salt.toByteArray(), 65536, 256)
//            val tmp: SecretKey = factory.generateSecret(spec)
//            val secretKey = SecretKeySpec(tmp.encoded, "AES")
//            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
//            cipher.parameters.getParameterSpec(IvParameterSpec::class.java).iv
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
//        }

        private fun getSalt(): String {
            val random = SecureRandom()
            val bytes = ByteArray(20)
            random.nextBytes(bytes)
            return String(bytes)
        }

        companion object {

            private fun getKey(myKey: String) : SecretKeySpec? {
                try {
                    var key = myKey.toByteArray(charset("UTF-8"))
                    val sha = MessageDigest.getInstance("SHA-256")
                    key = sha.digest(key)
                    key = key.copyOf(16)
                    return SecretKeySpec(key, "AES")
                } catch (e: NoSuchAlgorithmException) {
                    e.printStackTrace()
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }
                return null
            }

            fun enc(text: String, secret: String): String? {
                try {
                    val secretKey = getKey(secret) ?: return null
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    return Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray(charset("UTF-8"))))
                } catch (e: Exception) {
                    println("Error while encrypting: $e")
                }
                return null
            }

            fun dec(text: String, secret: String): String? {
                try {
                    val secretKey = getKey(secret) ?: return null
                    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey)
                    return String(cipher.doFinal(Base64.getDecoder().decode(text)))
                } catch (e: Exception) {
                    println("Error while encrypting: $e")
                }
                return null
            }

            fun encrypt(text: String, secret: String): String? {
                try {
                    val salt = "abc@email.com"
                    val iv = byteArrayOf(0, 1, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1)
                    val ivSpec = IvParameterSpec(iv)
                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    val spec: KeySpec = PBEKeySpec(secret.toCharArray(), salt.toByteArray(), 65536, 256)
                    val tmp: SecretKey = factory.generateSecret(spec)
                    val secretKey = SecretKeySpec(tmp.encoded, "AES")
                    val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
                    return Base64.getEncoder().encodeToString(cipher.doFinal(text.toByteArray(charset("UTF-8"))))
                } catch (e: Exception) {
                    println("Error while encrypting: $e")
                }
                return null
            }

            fun decrypt(text: String, secret: String): String? {
                try {
                    val salt = "abc@email.com"
//                    val iv = ByteArray(16)
//                    SecureRandom().nextBytes(iv)
                    val iv = byteArrayOf(0, 1, 2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1)
                    val ivSpec = IvParameterSpec(iv)
                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    val spec: KeySpec = PBEKeySpec(secret.toCharArray(), salt.toByteArray(), 65536, 256)
                    val tmp = factory.generateSecret(spec)
                    val secretKey = SecretKeySpec(tmp.encoded, "AES")
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
                    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
                    return String(cipher.doFinal(Base64.getDecoder().decode(text)))
                } catch (e: Exception) {
                    println("Error while decrypting: $e")
                }
                return null
            }
        }


    }

    fun fromHexString(hex: String): String {
        val str = StringBuilder()
        var i = 0
        while (i < hex.length) {
            str.append(hex.substring(i, i + 2).toInt(16).toChar())
            i += 2
        }
        return str.toString()
    }

    var disposable: Disposable? = null
    @Test
    fun testRx() {
        val array = intArrayOf(1, 2, 3)
//        Observable.create(ObservableOnSubscribe<Int> {
//            for (i in 0..10) {
//                Thread.sleep(500)
//                it.onNext(i)
//            }
//            if (!it.isDisposed) {
//                it.onComplete()
//            }
//        }).toList().subscribe({
//            println(it)
//        }, {
//
//        })

//        Flowable.create(FlowableOnSubscribe<Int> {
//            for (i in 0..10) {
//                Thread.sleep(500)
//                it.onNext(i)
//            }
//            if (!it.isCancelled) {
//                it.onComplete()
//            }
//        }, BackpressureStrategy.MISSING).subscribe({
//            println(it)
//        }, {
//
//        })

//        disposable =
//            Observable.interval(2, 2, TimeUnit.SECONDS)
//            .map {
//                5L
//            }.subscribe {
//                println(it)
//            }
        val aStrings = arrayOf("A1", "A2", "A3", "A4")
        val bStrings = arrayOf("B1", "B2", "B3")
        val cObservable = Observable.create(ObservableOnSubscribe<String> {
            for (i in 0..10) {
                it.onNext("C $i")
                Thread.sleep(500)
//                if (i == 4)
//                throw Throwable()
            }
            if (!it.isDisposed) {
                it.onComplete()
            }
        }).onErrorResumeNext(Observable.just("onErrorResumeNext"))

        val dObservable = Observable.create(ObservableOnSubscribe<String> {
            for (i in 0..10) {
                it.onNext("D $i")
                Thread.sleep(1000)
//                if (i == 4)
//                throw Throwable()
            }
            if (!it.isDisposed) {
                it.onComplete()
            }
        }).onErrorResumeNext(Observable.just("onErrorResumeNext"))
//        val aObservable = Observable.fromArray(*aStrings)
//        val bObservable = Observable.fromArray(*bStrings)
//
//        Observable.concat(aObservable, cObservable)
//            .doFinally { println("doFinally") }
//            .subscribe({
//                println("subscribe = " + it)
//            }, {
//                println("error")
//            }, {
//                println("completed")
//            })

   Observable.merge(cObservable, dObservable)
            .doFinally { println("doFinally") }
            .subscribe({
                println("subscribe = " + it)
            }, {
                println("error")
            }, {
                println("completed")
            })

    }

    private fun getObserver(): Observer<String> {
        return object : Observer<String> {
            override fun onSubscribe(d: Disposable) {
                Log.d("Result", " onSubscribe : " + d.isDisposed)
            }

            override fun onNext(value: String) {
                Log.d("Result", " onNext : value : $value")
            }

            override fun onError(e: Throwable) {
                Log.d("Result", " onError : " + e.message)
            }

            override fun onComplete() {
                Log.d("Result", " onComplete")
            }
        }
    }

    @Test
    fun testCalendar() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 1608915600000
        cal.add(Calendar.WEEK_OF_YEAR, 1)
//        cal.add(Calendar.WEEK_OF_YEAR, 1)
//        when(cal.get(Calendar.DAY_OF_WEEK)) {
//            Calendar.FRIDAY, Calendar.SATURDAY -> cal.add(Calendar.DATE, 1)
//            Calendar.SUNDAY -> {
//                cal.add(Calendar.DATE, 6) // SATURDAY
//            }
//            else -> cal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
//        }
//        val hour = 7 + Random().nextInt(15) // from 7h to 21h
//        cal.set(Calendar.HOUR_OF_DAY, hour)
//        cal.set(Calendar.MINUTE, 0)
//        val delay = cal.timeInMillis - Calendar.getInstance().timeInMillis
        println(cal.time)
        println(cal.get(Calendar.WEEK_OF_YEAR))
        println("\uD83D\uDC8CHình".replace(Constants.EMOJIS_REGEX.toRegex(), ""))
    }

    @Test
    fun testDomain() {
        val text = "https://iamhere.com.vn/wallstorage/minthumbnails/14_Other/23_20210312/Others210309011.jpg"
//        val xxx = Pattern.compile("(https.+?)/").matcher(text)
//        if (xxx.find()) print()
        println(text.replaceFirst("(http.+//.+?)/".toRegex(), "https://storageus.hotgirlwallpaper.xyz/"))

    }

    private fun getFailedDomain(url: String): String {
        if (url.contains("rest/")) {
            return url.replaceFirst("(http.*)rest/".toRegex(), "https://wallfaild.tpserverfaild.xyz/wallpapergz/rest/") + "&error=true"
        } else if (url.contains("wallpaper/restcache/")) {
            return url.replaceFirst("(http.*)restcache/".toRegex(), "https://wallvideo.tpserverfaild.xyz/wallpaper/restcache/") + "&error=true"
        }
        return ""
    }
}
