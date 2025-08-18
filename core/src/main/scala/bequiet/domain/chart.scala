package bequiet.domain

import doobie.Meta

object chart:
  enum Playstyle:
    case SP, DP

  object Playstyle:
    def fromString(s: String): Playstyle =
      Map(
        "SP" -> SP,
        "DP" -> DP
      )(s)
    def toString(p: Playstyle): String =
      Map(
        SP -> "SP",
        DP -> "DP"
      )(p)
    given Meta[Playstyle] = Meta[String].timap(fromString)(toString)

  enum Difficulty:
    case B, N, H, A, L

  object Difficulty:
    def fromString(s: String): Difficulty =
      Map(
        "B" -> B,
        "N" -> N,
        "H" -> H,
        "A" -> A,
        "L" -> L
      )(s)
    def toString(p: Difficulty): String =
      Map(
        B -> "B",
        N -> "N",
        H -> "H",
        A -> "A",
        L -> "L"
      )(p)
    given Meta[Difficulty] = Meta[String].timap(fromString)(toString)

  type ChartId = Int
  case class Chart(
      id: ChartId,
      playstyle: Playstyle,
      difficulty: Difficulty
      // notecount: Int
  )

  object Chart:
    given Conversion[Chart, ChartId] = _.id
