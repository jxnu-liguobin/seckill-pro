package io.github.service.test

import io.github.BaseTest
import io.github.seckillPro.entity.SeckillUser
import io.github.seckillPro.service.{GoodsService, OrderService}
import io.github.seckillPro.util.MD5Util

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 *
 * @author 梦境迷离
 * @time 2019-08-05
 * @version v2.0
 */
object OrderServiceTest extends BaseTest with App {

  val seckillOrder = Await.result(OrderService.getSeckillOrderByUserIdGoodsId(1, 1), Duration.Inf)
  println(seckillOrder)

  val goodsVo = Await.result(GoodsService.getGoodsVoByGoodsId(1), Duration.Inf)
  println("goodsVo => " + goodsVo)
  val createOrder = Await.result(OrderService.createOrder(SeckillUser(Option(15312345678L), "user",
    MD5Util.inputPassToDbPass("123456", "1a2b3c"), "1a2b3c", "", 1), goodsVo.get), Duration.Inf)
  println(createOrder)

  val order = Await.result(OrderService.getOrderById(1), Duration.Inf)
  println(order)

}