package com.xiangmuguanli.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "task_dependencies", indexes = {
    @Index(name = "idx_dep_task", columnList = "task_id"),
    @Index(name = "idx_dep_depend", columnList = "depend_task_id")
})
public class TaskDependency extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depend_task_id", nullable = false)
    private Task dependOnTask;

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Task getDependOnTask() {
        return dependOnTask;
    }

    public void setDependOnTask(Task dependOnTask) {
        this.dependOnTask = dependOnTask;
    }
}
