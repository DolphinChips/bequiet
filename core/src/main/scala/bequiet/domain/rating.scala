package bequiet.domain

import bequiet.domain.score.Lamp
import bequiet.domain.chart.ChartId

object rating:
  case class Rating(
      chartId: ChartId,
      lamp: Lamp,
      rating: Float
  )
