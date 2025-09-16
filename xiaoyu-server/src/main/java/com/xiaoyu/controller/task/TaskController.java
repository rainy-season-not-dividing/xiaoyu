package com.xiaoyu.controller.task;


import com.xiaoyu.dto.task.PublishTaskDTO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.TasksService;
import com.xiaoyu.vo.task.GetTasksVO;
import com.xiaoyu.vo.task.PublishTaskVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/tasks")
public class TaskController {

    @Resource
    private TasksService tasksService;

    @PostMapping
    public Result<PublishTaskVO> publishTask(PublishTaskDTO publishTaskDTO){
        log.info("发布任务：{}",publishTaskDTO);
        return Result.success(tasksService.publishTask(publishTaskDTO));
    }

    @GetMapping
    public Result<PageResult<GetTasksVO>> getTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size,
            @RequestParam(required=false) String status,    // 任务状态
            @RequestParam(required=false) String keyword,   // 关键字，关键字搜索，标题或内容
            @RequestParam(required=false) Integer tagId     // 标签ID
            ){
        log.info("获取任务列表");
        return Result.success(tasksService.getTasks(page,size,status,keyword,tagId));
    }

    @GetMapping("/my-published")
    public Result<PageResult<GetTasksVO>> getMyPublishedTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取我发布的任务列表");
        return Result.success(tasksService.getMyPublishedTasks(page,size));
    }

    @GetMapping("/my-received")
    public Result<PageResult<GetTasksVO>> getMyReceivedTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取我接取的任务列表");
        return Result.success(tasksService.getMyReceivedTasks(page,size));
    }

    @GetMapping("/{task_id}")
    public Result<GetTasksVO> getTask(@PathVariable Long taskId){
        log.info("获取任务详情：{}",taskId);
        return Result.success(tasksService.getTask(taskId));
    }


    @PutMapping("/{task_id}")
    public Result<Map<String,Object>> updateTask(@PathVariable Long taskId, @RequestBody PublishTaskDTO newTaskDTO){
        log.info("更新任务：{}",taskId);
        return Result.success(tasksService.updateTask(taskId,newTaskDTO));
    }

    @DeleteMapping("/{task_id}")
    public Result deleteTask(@PathVariable Long taskId){
        log.info("删除任务：{}",taskId);
        tasksService.removeTask(taskId);
        return Result.success("任务删除成功");
    }

    @PostMapping("/{task_id}/favorite")
    public Result favoriteTask(@PathVariable Long taskId){
        log.info("收藏任务：{}",taskId);
        tasksService.favoriteTask(taskId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{task_id}/favorite")
    public Result removeFavoriteTask(@PathVariable Long taskId){
        log.info("取消收藏任务：{}",taskId);
        tasksService.removeFavoriteTask(taskId);
        return Result.success("取消收藏成功");
    }

    // todo: 转发任务接口待完成

}
