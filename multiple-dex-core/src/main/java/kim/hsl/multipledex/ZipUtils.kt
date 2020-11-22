package kim.hsl.multipledex

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile

/**
 * 删除文件, 如果有目录, 则递归删除
 */
private fun deleteFile(file: File) {
    if (file.isDirectory) {
        val files = file.listFiles()
        for (f in files) {
            deleteFile(f)
        }
    } else {
        file.delete()
    }
}

/**
 * 解压文件
 * @param zip 被解压的压缩包文件
 * @param dir 解压后的文件存放目录
 */
fun unZipApk(zip: File, dir: File) {
    try {
        // 如果存放文件目录存在, 删除该目录
        deleteFile(dir)
        // 获取 zip 压缩包文件
        val zipFile = ZipFile(zip)
        // 获取 zip 压缩包中每一个文件条目
        val entries = zipFile.entries()
        // 遍历压缩包中的文件
        while (entries.hasMoreElements()) {
            val zipEntry = entries.nextElement()
            // zip 压缩包中的文件名称 或 目录名称
            val name = zipEntry.name
            // 如果 apk 压缩包中含有以下文件 , 这些文件是 V1 签名文件保存目录 , 不需要解压 , 跳过即可
            if (name == "META-INF/CERT.RSA" || name == "META-INF/CERT.SF" || (name
                        == "META-INF/MANIFEST.MF")
            ) {
                continue
            }
            // 如果该文件条目 , 不是目录 , 说明就是文件
            if (!zipEntry.isDirectory) {
                val file = File(dir, name)
                // 创建目录
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                // 向刚才创建的目录中写出文件
                val fileOutputStream = FileOutputStream(file)
                val inputStream = zipFile.getInputStream(zipEntry)
                val buffer = ByteArray(1024)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    fileOutputStream.write(buffer, 0, len)
                }
                inputStream.close()
                fileOutputStream.close()
            }
        }

        // 关闭 zip 文件
        zipFile.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}