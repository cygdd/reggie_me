package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;

import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import com.itheima.reggie.service.impl.DishServiceImpl;
import com.itheima.reggie.service.impl.SetmealServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private DishServiceImpl  dishService;

    @Autowired
    private SetmealServiceImpl setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
/*        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);

        *//**
         * 注意如果这里直接返回R.success(pageInfo)，
         * 虽然不会报错但是分页的数据的套餐分类的名字是显示不了的；
         * 因为这个分页的泛型是Setmeal,Setmeal只封装了f分类的Id categoryId，没有分类的名称 name
         * 所以又需要进行name的获取和设值
         *//*
        注意，修改套餐的话，需要先对菜品页面进行填充，这一页都是需要填充满要修改的菜品信息的。*/

        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }



// 其实还可以整合成一个方法，url写为/status，读取后面的值设置为status，根据status对ids进行操作即可

    /**
     * 批量起售停售
     * @param ids
     * @return
     */


    @PostMapping("/status/0")
    public R<String> closeStatus(@RequestParam List<Long> ids){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);

        for (Setmeal setmeal:setmeals
        ) {
            setmeal.setStatus(0);
            setmealService.updateById(setmeal);
        }

        return R.success("修改成功");
    }

    @PostMapping("/status/1")
    public R<String> openStatus(@RequestParam List<Long> ids){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);

        for (Setmeal setmeal:setmeals
        ) {
            setmeal.setStatus(1);
            setmealService.updateById(setmeal);
        }

        return R.success("修改成功");
    }



    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){

        // 我们需要把setmealDto返回回去，定义一个新的setmealDto用于保存数据
        SetmealDto setmealDto = new SetmealDto();

        // 将普通数据传入

        Setmeal setmeal = setmealService.getById(id);

        BeanUtils.copyProperties(setmeal,setmealDto);

        // 将菜品信息传递进去

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);

        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        // 返回setmealDto即可
        return R.success(setmealDto);
    }
    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){

        setmealService.updateById(setmealDto);

        return R.success("修改成功");

    }


    /**
    *
     * 用户端
    *
    * */
    /**
     * 点击套餐图片查看套餐具体内容
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param SetmealId
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dish(@PathVariable("id") Long SetmealId) {

        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, SetmealId);

        // 获得数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();

            // 基本信息拷贝
            BeanUtils.copyProperties(setmealDish, dishDto);

            // 设置其他信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);
    }
}
