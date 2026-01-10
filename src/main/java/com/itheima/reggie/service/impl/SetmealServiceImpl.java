package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LJM
 * @create 2022/4/16
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal,执行insert
        this.save(setmealDto);
        log.info(setmealDto.toString()); //查看一下这个套餐的基本信息是什么

        //保存套餐和菜品的关联信息，操作setmeal_dish ,执行insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //注意上面拿到的setmealDishes是没有setmeanlId这个的值的，通过debug可以发现
        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item; //这里返回的就是集合的泛型
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes); //批量保存
    }
    /**
     * 带菜品关联一同删除
     * @param ids
     */
    public void removeWithDish(List<Long> ids){

        // 判断是否是停售状态，若不为停售不能删除，抛出业务异常
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);

        if (count > 0){
            throw new CustomException("删除业务中有套餐处于启售状态，无法删除");
        }

        // 可以删除后执行删除操作

        // 先删除套餐
        this.removeByIds(ids);

        // 再删除套餐关联信息
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lambdaQueryWrapper);

    }

    /**
     * 修改操作
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto){

        // 首先修改套餐上的信息
        this.updateById(setmealDto);

        // 修改内部菜品操作（同样先删除再添加）

        // 删除操作
        Long setmealId = setmealDto.getId();

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);

        setmealDishService.remove(queryWrapper);

        // 新填操作

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishService.saveBatch(setmealDishes);

    }
}
