package com.xiaoyu.controller.task;


import com.xiaoyu.dto.task.PublishTaskDTO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.yujiTasksService;
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
    private yujiTasksService yujiTasksService;

    @PostMapping
    public Result<PublishTaskVO> publishTask(@RequestBody PublishTaskDTO publishTaskDTO){
        log.info("发布任务：{}",publishTaskDTO);
        return Result.success(yujiTasksService.publishTask(publishTaskDTO));
    }

    @GetMapping
    public Result<PageResult<GetTasksVO>> getTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size,
            @RequestParam(required=false) TasksPO.Status status,    // 任务状态
            @RequestParam(required=false) String keyword,   // 关键字，关键字搜索，标题或内容
            @RequestParam(required=false) Integer tagId     // 标签ID
            ){
        // todo: 返回的任务中不能有”审核中“的状态
        log.info("获取任务列表");
        return Result.success(yujiTasksService.getTasks(page,size,status,keyword,tagId));
    }

    @GetMapping("/my-published")
    public Result<PageResult<GetTasksVO>> getMyPublishedTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取我发布的任务列表");
        return Result.success(yujiTasksService.getMyPublishedTasks(page,size));
    }

    @GetMapping("/my-received")
    public Result<PageResult<GetTasksVO>> getMyReceivedTasks(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取我接取的任务列表");
        return Result.success(yujiTasksService.getMyReceivedTasks(page,size));
    }

    @GetMapping("/{taskId}")
    public Result<GetTasksVO> getTask(@PathVariable Long taskId) throws InterruptedException{
        log.info("获取任务详情：{}",taskId);
        return Result.success(yujiTasksService.getTask(taskId));
    }


    @PutMapping("/{taskId}")
    public Result<Map<String,Object>> updateTask(@PathVariable Long taskId, @ModelAttribute PublishTaskDTO newTaskDTO){
        log.info("更新任务：{}",taskId);
        return Result.success(yujiTasksService.updateTask(taskId,newTaskDTO));
    }

    @DeleteMapping("/{taskId}")
    public Result deleteTask(@PathVariable Long taskId){
        log.info("删除任务：{}",taskId);
        yujiTasksService.removeTask(taskId);
        return Result.success("任务删除成功");
    }

    @PostMapping("/{taskId}/favorite")
    public Result favoriteTask(@PathVariable Long taskId){
        log.info("收藏任务：{}",taskId);
        yujiTasksService.favoriteTask(taskId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/{taskId}/favorite")
    public Result removeFavoriteTask(@PathVariable Long taskId){
        log.info("取消收藏任务：{}",taskId);
        yujiTasksService.removeFavoriteTask(taskId);
        return Result.success("取消收藏成功");
    }

    // todo: 转发任务接口待完成

}
