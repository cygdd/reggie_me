package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据:{}",shoppingCart);

        //1.设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();
        // 2. 判断是否是第一次加入，若第一次直接添加，若不是第一次number++
        // 2.2 给部分判断条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if(dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //SQL:select * from shopping_cart where user_id = ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);//查询当前菜品或者套餐是否在购物车中


        // 2.3判断是否是第一次加入
        if(cartServiceOne != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
// 进行用户比对
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        // 查询并返回即可
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?
// 进行用户比对
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
// 删除即可
        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }

    /**
     * 数值删减
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        // 设置用户
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 基本查找条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        Long dishId = shoppingCart.getDishId();

        // 判断菜品或者套餐

        if(dishId != null){
            // 购物车为菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else {
            // 购物车为套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        // 判断个数，如果为正数，减一；如果为0，抛出异常
        ShoppingCart shoppingCartServiceOne = shoppingCartService.getOne(queryWrapper);
        Integer number = shoppingCartServiceOne.getNumber();

        if (number != 0){
            shoppingCartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(shoppingCartServiceOne);
        }else {
            shoppingCartService.removeById(shoppingCartServiceOne);
        }

        return R.success(shoppingCartServiceOne);
    }
//客户端点击再来一单
    /**
     * 我们需要将订单内的菜品重新加入购物车，所以在此之前我们需要将购物车清空（业务层实现方法）
     *
     *
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        // 获得ID
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        // 制作判断条件
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);

        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空
        this.clean();
//        shoppingCartService.clean();

        //获取用户id
        Long userId = BaseContext.getCurrentId();

        // 整体赋值
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {

            // 以下均为赋值操作

            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());

            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();

            if (dishId != null) {
                // 如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                // 添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        // 将携带数据的购物车批量插入购物车表
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }

}
