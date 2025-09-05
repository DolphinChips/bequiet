package bequiet.services

import bequiet.domain.song.*

trait Songs[F[_]]:
  def create(id: SongId, artist: String, title: String): F[Unit]
  def all: fs2.Stream[F, Song]
  def find(id: Int): F[Option[Song]]
  def find(title: String): F[Option[Song]]

object Songs:
  def apply[F[_]](using songs: Songs[F]) = songs
