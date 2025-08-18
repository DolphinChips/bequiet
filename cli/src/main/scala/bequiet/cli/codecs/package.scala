package bequiet.cli.codecs

import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.constantLenient

def reserved(bytes: Int): Codec[Unit] = constantLenient(
  BitVector.high(bytes.toLong * 8)
)
