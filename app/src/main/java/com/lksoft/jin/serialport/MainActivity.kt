package com.lksoft.jin.serialport

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.lksoft.jin.serialporttool.SerialPort
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val serialPort = SerialPort(File("/dev/ttyS4"), 9600, 8, 1, 'N')
        thread {
            val writeByteArray = getModBusCRCHexStr("030400000002", isSplice = true).toHexByteArray()
            if (writeByteArray != null) {
                serialPort.outputStream?.write(writeByteArray)
                Thread.sleep(500L)
                val byteArray = ByteArray(128)
                serialPort.inputStream?.read(byteArray)
                Log.i("Jin", byteArrayToHexString(byteArray))
            }
        }
    }

    private fun byteArrayToHexString(byteArray: ByteArray): String? {
        val stringBuilder = StringBuilder("")
        if (byteArray.isEmpty()) {
            return null
        }
        for (i in 0 until byteArray.size) {
            val v = byteArray[i].toInt() and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    /**
     * String 扩展方法
     * 将字符串转为 十六进制 ByteArray
     */
    fun String.toHexByteArray(): ByteArray? {
        if (this == "") {
            return null
        }
        val hexString = this.toUpperCase()
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] = (hexChars[pos].toHexByte().toInt() shl 4 or hexChars[pos + 1].toHexByte().toInt()).toByte()
        }
        return d
    }

    /**
     * 十六进制字符串转 Int[]
     */
    private fun strToToHexIntArray(hexString: String): IntArray {
        var tempHexString = hexString
        tempHexString = tempHexString.replace("", "")
        // 如果长度不是偶数，那么后面添加空格。

        if (tempHexString.length % 2 != 0) {
            tempHexString += " "
        }

        // 定义数组，长度为待转换字符串长度的一半。
        val returnBytes = IntArray(tempHexString.length / 2)

        for (i in returnBytes.indices)
        // 这里为什么会出现负数呢?
            returnBytes[i] = 0xff and Integer.parseInt(tempHexString.substring(i * 2, i * 2 + 2), 16)
        return returnBytes
    }

    /**
     * 计算 ModBus 校验位 (十进制)
     *
     * @param DATA ModBus 命令字符串
     * @return ModBus 校验位(十进制)
     */
    private fun getModBusCRC(DATA: String): Long {
        val functionReturnValue: Long
        var i: Long = 0
        var j: Long
        // 之前之所以错误, 是因为有的数字被认为是负数了.
        val v = strToToHexIntArray(DATA)
        var crc: Long
        crc = 0xffffL
        while (i <= v.size - 1) { // 2. 把第一个 8 位二进制数据（既通讯信息帧的第一个字节）与 16 位的 CRC 寄存器的低 8 位相异或，把结果放于 CRC 寄存器；
            crc = crc / 256 * 256L + crc % 256L xor v[i.toInt()].toLong()
            j = 0
            while (j <= 7) { // 3. 把 CRC 寄存器的内容右移一位（朝低位）用 0 填补最高位，并检查最低位；
                // 4. 如果最低位为 0：重复第 3 步（再次右移一位）；
                // 如果最低位为 1：CRC 寄存器与多项式 A001（1010 0000 0000 0001）进行异或；
                // 5. 重复步骤 3 和 4，直到右移 8 次，这样整个 8 位数据全部进行了处理；
                val d0: Long = crc and 1L
                crc /= 2
                if (d0 == 1L)
                    crc = crc xor 0xa001L
                j++
            } // 6. 重复步骤 2 到步骤 5，进行通讯信息帧下一字节的处理；
            i++
        } // 7. 最后得到的 CRC 寄存器内容即为：CRC 码。
        crc %= 65536
        functionReturnValue = crc
        return functionReturnValue
    }

    private fun getModBusCRCHexStr(DATA: String, isSplice: Boolean): String {
        val crcTemp = StringBuilder(java.lang.Long.toHexString(getModBusCRC(DATA)))
        while (crcTemp.length < 4) {
            crcTemp.insert(0, "0")
        }
        val crc = crcTemp.substring(2, 4) + crcTemp.substring(0, 2)
        return if (isSplice) {
            DATA + crc.toUpperCase()
        } else {
            crc.toUpperCase()
        }
    }

    /**
     * Char 扩展方法
     * 将 Char 转换为 十六进制 Byte
     */
    fun Char.toHexByte(): Byte {
        return "0123456789ABCDEF".indexOf(this).toByte()
    }
}
