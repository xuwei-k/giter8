package giter8

object GIO {
  import java.io.{File,
                  FileInputStream => FIS,
                  InputStream => IS,
                  FileOutputStream => FOS,
                  OutputStream => OS,
                  ByteArrayOutputStream => BOS,
                  ByteArrayInputStream => BIS
                }

  def use[C<:{ def close(): Unit}, T](c: C)(f: C => T) = try {
    f(c)
  } finally {
    c.close
  }

  @annotation.tailrec
  def transfer(fis: IS, fos: OS, buf: Array[Byte]): Unit = {
    fis.read(buf, 0, buf.length) match {
      case -1 =>
        fos.flush()
      case read =>
         fos.write(buf, 0, read)
         transfer(fis, fos, buf)
    }
  }

  def read(from: File, charset: String) = {
    val bos = new BOS()
    use(new FIS(from)) { in =>
      use(bos) { out =>
        transfer(in, out, new Array[Byte](1024*16))
      }
    }
    bos.toString(charset)
  }    
  def copyFile(from: File, to: File) = {
    to.getParentFile().mkdirs()
    use(new FIS(from)) { in =>
      use(new FOS(to)) { out =>
        transfer(in, out, new Array[Byte](1024*16))
      }
    }
  }
  def write(to: File, from: String, charset: String) = {
    use(new BIS(from.getBytes(charset))) { in =>
      use(new FOS(to)) { out =>
        transfer(in, out, new Array[Byte](1024*16))
      }
    }
  }
}
