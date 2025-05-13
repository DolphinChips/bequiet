package bequiet.domain.chart

enum Playstyle:
  case SP, DP

enum Difficulty:
  case B, N, H, A, L

type ChartId = Int
case class Chart(
    id: ChartId,
    playstyle: Playstyle,
    difficulty: Difficulty,
    notecount: Int
)

object Chart:
  given Conversion[Chart, ChartId] = _.id
