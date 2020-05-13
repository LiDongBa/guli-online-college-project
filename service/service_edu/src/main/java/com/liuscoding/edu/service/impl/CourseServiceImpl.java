package com.liuscoding.edu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuscoding.edu.entity.Course;
import com.liuscoding.edu.entity.CourseDescription;
import com.liuscoding.edu.entity.Subject;
import com.liuscoding.edu.enums.EduResultCode;
import com.liuscoding.edu.mapper.CourseMapper;
import com.liuscoding.edu.model.form.CourseInfoForm;
import com.liuscoding.edu.model.vo.CourseInfoVo;
import com.liuscoding.edu.service.CourseDescriptionService;
import com.liuscoding.edu.service.CourseService;
import com.liuscoding.edu.service.SubjectService;
import com.liuscoding.servicebase.exceptionhandler.exception.GuliException;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * <p>
 * 课程 服务实现类
 * </p>
 *
 * @author liusCoding
 * @since 2020-05-10
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseDescriptionService courseDescriptionService;
    private final SubjectService subjectService;

    public CourseServiceImpl(CourseDescriptionService courseDescriptionService, SubjectService subjectService) {
        this.courseDescriptionService = courseDescriptionService;
        this.subjectService = subjectService;
    }

    /**
     * 保存课程信息
     *
     * @param courseInfoForm 课程信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveCourseInfo(CourseInfoForm courseInfoForm) {
        //1.向课程表添加课程基本信息
        Subject subject = subjectService.getById(courseInfoForm.getSubjectId());
        subject = Optional.ofNullable(subject).orElseThrow(() -> GuliException.from(EduResultCode.DATA_NO_EXIST));

        Course course = new Course();
        BeanUtils.copyProperties(courseInfoForm,course);
        course.setSubjectParentId(subject.getParentId());
        boolean saveResult = this.save(course);
        if(!saveResult){
            //添加失败，提示用户
            throw GuliException.from(EduResultCode.SAVE_ERROR);
        }

        //获取添加之后的课程id
        String courseId = course.getId();

        //2.向课程简介表添加简介
        CourseDescription courseDescription = new CourseDescription();
        courseDescription.setDescription(courseInfoForm.getDescription());
        courseDescription.setId(courseId);
        boolean saveCourseDescResult = courseDescriptionService.save(courseDescription);
        if (!saveCourseDescResult){
            throw GuliException.from(EduResultCode.SAVE_ERROR);
        }


        return courseId;
    }

    /**
     * 根据课程id 查询课程信息
     *
     * @param courseId 课程id
     * @return CourseInfoVo
     */
    @Override
    public CourseInfoVo getCourseInfoByCourseId(String courseId) {

        //1.查询课程信息
        Course course = this.getById(courseId);
        CourseInfoVo courseInfoVo = new CourseInfoVo();

        BeanUtils.copyProperties(course,courseInfoVo);

        //2.查询课程描述信息
        CourseDescription courseDescription = courseDescriptionService.getById(courseId);
        courseInfoVo.setDescription(courseDescription.getDescription());
        return courseInfoVo;
    }

    /**
     * 修改课程信息
     *
     * @param courseInfoForm
     */
    @Override
    public void updateCourse(CourseInfoForm courseInfoForm) {
        //1.修改课程表
        Course course = new Course();
        BeanUtils.copyProperties(courseInfoForm,course);
        boolean updateResult = updateById(course);
        if(!updateResult){
            throw GuliException.from(EduResultCode.UPDATE_ERROR);
        }
        
        //2.修改描述表
        CourseDescription courseDescription = new CourseDescription(courseInfoForm.getId(),courseInfoForm.getDescription());
        boolean updateDescResult = courseDescriptionService.updateById(courseDescription);

        if (!updateDescResult){
            throw GuliException.from(EduResultCode.UPDATE_ERROR);
        }

    }
}