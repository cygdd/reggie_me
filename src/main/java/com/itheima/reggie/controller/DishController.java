package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
/*        // 构造基本Page
        Page<Dish> pageImpl = new Page<>(page,pageSize);

        // 进行查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(name != null,Dish::getName,name);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        // 查询赋值，此时pageImpl里有值
        dishService.page(pageImpl,queryWrapper);

        return Result.success(pageImpl);*/
//        此时我们出来的页面中是无法查看到分类所属的：
//        所以我们需要设置包含有菜品分类名称的实体类作为Page的实现类参数才可以将菜品分类的名称传递到前端
//        我们只需要到前端代码中查看就可以注意到，商品分类这行上的数据属性名称为categoryName，所以我们采用DishDto来完成操作

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);
        // 但是我们需要返回DishDto类型的Page，我们将pageImpl的值赋值到dishDtoPage中（不要赋值records，这个值是数据，我们需要单独处理）
        // 我们借助工具类实现
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        // 然后我们来处理dishDtoPage中的records值，我们首先将pageImpl的records提取出来
        List<Dish> records = pageInfo.getRecords();
        // 将该值除了CategoryName全都赋值给dishDtoPage的records(我们这里使用stream流进行局内单个赋值，采用foreach方法也相同)
        List<DishDto> list = records.stream().map((item) -> {
            // 创建一个新dishDto作为返回实体
            DishDto dishDto = new DishDto();
// 将正常属性赋值进去
            BeanUtils.copyProperties(item,dishDto);
            // 将CategoryName复制进去
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
// 完成dishDtoPage的results的内容封装
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("修改菜品成功");
    }
    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteByIds(Long[] ids){

        for (Long id:ids
        ) {
            dishService.removeById(id);
        }

        return R.success("删除成功");
    }

    /**
     * 批量启售
     * @param ids
     * @return
     */

    @PostMapping("/status/1")
    public R<String> openStatus(Long[] ids) {

        for (Long id : ids
        ) {
            Dish dish = dishService.getById(id);
            dish.setStatus(1);
            dishService.updateById(dish);
        }

        return R.success("修改成功");
    }


    /**
     * 批量停售
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public R<String> closeStatus(Long[] ids) {

        for (Long id : ids
        ) {
            Dish dish = dishService.getById(id);
            dish.setStatus(0);
            dishService.updateById(dish);
        }

        return R.success("修改成功");
    }
    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
/*    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){//会自动映射的
        //这里可以传categoryId,但是为了代码通用性更强,这里直接使用dish类来接受（因为dish里面是有categoryId的）,以后传dish的其他属性这里也可以使用
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/

    /*@GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        // 提取CategoryID
        Long id = dish.getCategoryId();

        // 判断条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,Dish::getCategoryId,id);
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort);

        List<Dish> list = dishService.list(queryWrapper);

        // 创建返回类型
        List<DishDto> dishDtoList = list.stream().map((item) -> {

            // 创建新的返回类型内部
            DishDto dishDto = new DishDto();

            // 将元素复制过去
            BeanUtils.copyProperties(item,dishDto);

            // 设置CategoryName
            Long categoryId = item.getCategoryId();

            LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
            categoryLambdaQueryWrapper.eq(Category::getId,categoryId);

            Category category = categoryService.getOne(categoryLambdaQueryWrapper);

            String categoryName = category.getName();

            dishDto.setCategoryName(categoryName);

            // 设置flavor
            Long dishId = item.getId();

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);

            List<DishFlavor> dishFlavors = dishFlavorService.list(lambdaQueryWrapper);

            dishDto.setFlavors(dishFlavors);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);

    }

}
