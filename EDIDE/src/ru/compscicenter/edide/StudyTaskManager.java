package ru.compscicenter.edide;

import com.intellij.openapi.components.*;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.compscicenter.edide.course.Course;
import ru.compscicenter.edide.course.Lesson;
import ru.compscicenter.edide.course.Task;
import ru.compscicenter.edide.course.TaskFile;

import java.util.HashMap;
import java.util.Map;

/**
 * User: lia
 * Date: 26.12.13
 * Time: 20:37
 */


@State(
  name = "StudySettings",
  storages = {
    @Storage(
      id = "others",
      file = "$PROJECT_CONFIG_DIR$/study_project.xml",
      scheme = StorageScheme.DIRECTORY_BASED
    )}
)
public class StudyTaskManager implements ProjectComponent, PersistentStateComponent<Element> {
  public static final String COURSE_ELEMENT = "courseElement";
  private static Map<String, StudyTaskManager> myTaskManagers = new HashMap<String, StudyTaskManager>();
  private final Project myProject;
  private Course myCourse;


  public void setCourse(Course course) {
    myCourse = course;
  }

  private StudyTaskManager(Project project) {
    myTaskManagers.put(project.getBasePath(), this);
    myProject = project;
  }


  public Course getCourse() {
    return myCourse;
  }

  @Nullable
  @Override
  public Element getState() {
    Element el = new Element("taskManager");
    Element courseElement = new Element(COURSE_ELEMENT);
    XmlSerializer.serializeInto(myCourse, courseElement);
    el.addContent(courseElement);
    return el;
  }

  @Override
  public void loadState(Element el) {
    myCourse = XmlSerializer.deserialize(el.getChild(COURSE_ELEMENT), Course.class);
    if (myCourse != null) {
      myCourse.init(true);
    }
  }

  @Override
  public void projectOpened() {
  }

  @Override
  public void projectClosed() {
  }

  @Override
  public void initComponent() {
    EditorFactory.getInstance().addEditorFactoryListener(new StudyEditorFactoryListener(), myProject);
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return "StudyTaskManager";
  }

  public static StudyTaskManager getInstance(Project project) {
    StudyTaskManager item = myTaskManagers.get(project.getBasePath());
    if (item != null) {
      return item;
    }
    return new StudyTaskManager(project);
  }

  private int getIndex(String fullName, String logicalName) {
    return Integer.parseInt(fullName.substring(logicalName.length())) - 1;
  }

  public TaskFile getTaskFile(VirtualFile file) {
    VirtualFile fileParent = file.getParent();
    if (fileParent != null) {
      String taskDirName = fileParent.getName();
      if (taskDirName.contains(Task.TASK_DIR)) {
        VirtualFile lessonDir = fileParent.getParent();
        if (lessonDir != null) {
          String lessonDirName = lessonDir.getName();
          int lessonIndex = getIndex(lessonDirName, Lesson.LESSON_DIR);
          Lesson lesson = myCourse.getLessons().get(lessonIndex);
          int taskIndex = getIndex(taskDirName, Task.TASK_DIR);
          Task task = lesson.getTaskList().get(taskIndex);
          return task.getFile(file.getName());
        }
      }
    }
    return null;
  }
}
