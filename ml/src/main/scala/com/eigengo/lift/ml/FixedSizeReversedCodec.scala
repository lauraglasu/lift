package com.eigengo.lift.ml

import scodec.Codec
import scodec.bits.BitVector

import scalaz.{\/-, -\/, \/}

final class FixedSizeReversedCodec[A](size: Long, codec: Codec[A]) extends Codec[A] {

  override def encode(a: A) = for {
    encoded <- codec.encode(a)
    result <- {
      if (encoded.size > size)
        \/.left(s"[$a] requires ${encoded.size} bits but field is fixed size of $size bits")
      else
        \/.right(encoded.padTo(size))
    }
  } yield result

  override def decode(buffer: BitVector) =
    buffer.acquire(size) match {
      case Left(e) => \/.left(e)
      case Right(b) =>
        codec.decode(b.reverseByteOrder) match {
          case e @ -\/(_) => e
          case \/-((rest, res)) => \/-((buffer.drop(size), res))
        }
    }

  override def toString = s"fixedSizeBits($size, $codec)"
}
