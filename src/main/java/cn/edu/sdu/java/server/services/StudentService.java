package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Service
public class StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private final PersonRepository personRepository;  //人员数据操作自动注入
    private final StudentRepository studentRepository;  //学生数据操作自动注入
    private final UserRepository userRepository;  //学生数据操作自动注入
    private final UserTypeRepository userTypeRepository; //用户类型数据操作自动注入
    private final PasswordEncoder encoder;  //密码服务自动注入
    private final FeeRepository feeRepository;  //消费数据操作自动注入
    private final FamilyMemberRepository familyMemberRepository;
    private final SystemService systemService;
    private final ScoreRepository scoreRepository;
    public StudentService(PersonRepository personRepository, StudentRepository studentRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, PasswordEncoder encoder, FeeRepository feeRepository, FamilyMemberRepository familyMemberRepository, SystemService systemService, ScoreRepository scoreRepository) {
        this.personRepository = personRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.encoder = encoder;
        this.feeRepository = feeRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.systemService = systemService;
        this.scoreRepository = scoreRepository;
    }

    public Map<String,Object> getMapFromStudent(Student s) {
        Map<String,Object> m = new HashMap<>();
        Person p;
        if(s == null)
            return m;
        m.put("major",s.getMajor());
        m.put("className",s.getClassName());
        p = s.getPerson();
        if(p == null)
            return m;
        m.put("personId", s.getPersonId());
        m.put("num",p.getNum());
        m.put("name",p.getName());
        m.put("dept",p.getDept());
        m.put("card",p.getCard());
        String gender = p.getGender();
        m.put("gender",gender);
        m.put("genderName", ComDataUtil.getInstance().getDictionaryLabelByValue("XBM", gender)); //性别类型的值转换成数据类型名
        m.put("birthday", p.getBirthday());  //时间格式转换字符串
        m.put("email",p.getEmail());
        m.put("phone",p.getPhone());
        m.put("address",p.getAddress());
        m.put("introduce",p.getIntroduce());
        return m;
    }

    //Java 对象的注入 我们定义的这下Java的操作对象都不能自己管理是由有Spring框架来管理的， StudentController 中要使用StudentRepository接口的实现类对象，
    // 需要下列方式注入，否则无法使用， studentRepository 相当于StudentRepository接口实现对象的一个引用，由框架完成对这个引用的赋值，
    // StudentController中的方法可以直接使用

    public List<Map<String,Object>> getStudentMapList(String numName) {
        List<Map<String,Object>> dataList = new ArrayList<>();
        List<Student> sList = studentRepository.findStudentListByNumName(numName);  //数据库查询操作
        List<Map<String,Object>> mList = studentRepository.findStudentMapListByNumName(numName);
        if (sList == null || sList.isEmpty())
            return dataList;
        for (Student student : sList) {
            dataList.add(getMapFromStudent(student));
        }
        return dataList;
    }

    public DataResponse getStudentList(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Map<String,Object>> dataList = getStudentMapList(numName);
        return CommonMethod.getReturnData(dataList);  //按照测试框架规范会送Map的list
    }



    public DataResponse studentDelete(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");  //获取student_id值
        Student s = null;
        Optional<Student> op;
        if (personId != null && personId > 0) {
            op = studentRepository.findById(personId);   //查询获得实体对象
            if(op.isPresent()) {
                s = op.get();
                Optional<User> uOp = userRepository.findById(personId); //查询对应该学生的账户
                //删除对应该学生的账户
                uOp.ifPresent(userRepository::delete);
                Person p = s.getPerson();
                studentRepository.delete(s);    //首先数据库永久删除学生信息
                personRepository.delete(p);   // 然后数据库永久删除学生信息
            }
        }
        return CommonMethod.getReturnMessageOK();  //通知前端操作正常
    }


    public DataResponse getStudentInfo(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Student s = null;
        Optional<Student> op;
        if (personId != null) {
            op = studentRepository.findById(personId); //根据学生主键从数据库查询学生的信息
            if (op.isPresent()) {
                s = op.get();
            }
        }
        return CommonMethod.getReturnData(getMapFromStudent(s)); //这里回传包含学生信息的Map对象
    }

    public DataResponse studentEditSave(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Map<String,Object> form = dataRequest.getMap("form"); //参数获取Map对象
        String num = CommonMethod.getString(form, "num");  //Map 获取属性的值
        Student s = null;
        Person p;
        User u;
        Optional<Student> op;
        boolean isNew = false;
        if (personId != null) {
            op = studentRepository.findById(personId);  //查询对应数据库中主键为id的值的实体对象
            if (op.isPresent()) {
                s = op.get();
            }
        }
        Optional<Person> nOp = personRepository.findByNum(num); //查询是否存在num的人员
        if (nOp.isPresent()) {
            if (s == null || !s.getPerson().getNum().equals(num)) {
                return CommonMethod.getReturnMessageError("新学号已经存在，不能添加或修改！");
            }
        }
        if (s == null) {
            p = new Person();
            p.setNum(num);
            p.setType("1");
            personRepository.saveAndFlush(p);  //插入新的Person记录
            personId = p.getPersonId();
            String password = encoder.encode("123456");
            u = new User();
            u.setPersonId(personId);
            u.setUserName(num);
            u.setPassword(password);
            u.setUserType(userTypeRepository.findByName(EUserType.ROLE_STUDENT.name()));
            u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            u.setCreatorId(CommonMethod.getPersonId());
            userRepository.saveAndFlush(u); //插入新的User记录
            s = new Student();   // 创建实体对象
            s.setPersonId(personId);
            studentRepository.saveAndFlush(s);  //插入新的Student记录
            isNew = true;
        } else {
            p = s.getPerson();
        }
        personId = p.getPersonId();
        if (!num.equals(p.getNum())) {   //如果人员编号变化，修改人员编号和登录账号
            Optional<User> uOp = userRepository.findByPersonPersonId(personId);
            if (uOp.isPresent()) {
                u = uOp.get();
                u.setUserName(num);
                userRepository.saveAndFlush(u);
            }
            p.setNum(num);  //设置属性
        }
        p.setName(CommonMethod.getString(form, "name"));
        p.setDept(CommonMethod.getString(form, "dept"));
        p.setCard(CommonMethod.getString(form, "card"));
        p.setGender(CommonMethod.getString(form, "gender"));
        p.setBirthday(CommonMethod.getString(form, "birthday"));
        p.setEmail(CommonMethod.getString(form, "email"));
        p.setPhone(CommonMethod.getString(form, "phone"));
        p.setAddress(CommonMethod.getString(form, "address"));
        personRepository.save(p);  // 修改保存人员信息
        s.setMajor(CommonMethod.getString(form, "major"));
        s.setClassName(CommonMethod.getString(form, "className"));
        studentRepository.save(s);  //修改保存学生信息
        systemService.modifyLog(s,isNew);
        return CommonMethod.getReturnData(s.getPersonId());  // 将personId返回前端
    }

    public List<Map<String,Object>> getStudentScoreList(List<Score> sList) {
        List<Map<String,Object>> list = new ArrayList<>();
        if (sList == null || sList.isEmpty())
            return list;
        Map<String,Object> m;
        Course c;
        for (Score s : sList) {
            m = new HashMap<>();
            c = s.getCourse();
            m.put("studentNum", s.getStudent().getPerson().getNum());
            m.put("scoreId", s.getScoreId());
            m.put("courseNum", c.getNum());
            m.put("courseName", c.getName());
            m.put("credit", c.getCredit());
            m.put("mark", s.getMark());
            m.put("ranking", s.getRanking());
            list.add(m);
        }
        return list;
    }


    public List<Map<String,Object>> getStudentMarkList(List<Score> sList) {
        String[] title = {"优", "良", "中", "及格", "不及格"};
        int[] count = new int[5];
        List<Map<String,Object>> list = new ArrayList<>();
        if (sList == null || sList.isEmpty())
            return list;
        Map<String,Object> m;
        Course c;
        for (Score s : sList) {
            c = s.getCourse();
            if (s.getMark() >= 90)
                count[0]++;
            else if (s.getMark() >= 80)
                count[1]++;
            else if (s.getMark() >= 70)
                count[2]++;
            else if (s.getMark() >= 60)
                count[3]++;
            else
                count[4]++;
        }
        for (int i = 0; i < 5; i++) {
            m = new HashMap<>();
            m.put("name", title[i]);
            m.put("title", title[i]);
            m.put("value", count[i]);
            list.add(m);
        }
        return list;
    }


    public List<Map<String,Object>> getStudentFeeList(Integer personId) {
        List<Fee> sList = feeRepository.findListByStudent(personId);  // 查询某个学生消费记录集合
        List<Map<String,Object>> list = new ArrayList<>();
        if (sList == null || sList.isEmpty())
            return list;
        Map<String,Object> m;
        Course c;
        for (Fee s : sList) {
            m = new HashMap<>();
            m.put("title", s.getDay());
            m.put("value", s.getMoney());
            list.add(m);
        }
        return list;
    }




    public String importFeeData(Integer personId, InputStream in){
        try {
            Student student = studentRepository.findById(personId).get();
            XSSFWorkbook workbook = new XSSFWorkbook(in);  //打开Excl数据流
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Row row;
            Cell cell;
            int i;
            i = 1;
            String day, money;
            Optional<Fee> fOp;
            double dMoney;
            Fee f;
            rowIterator.next();
            while (rowIterator.hasNext()) {
                row = rowIterator.next();
                cell = row.getCell(0);
                if (cell == null)
                    break;
                day = cell.getStringCellValue();  //获取一行消费记录 日期 金额
                cell = row.getCell(1);
                money = cell.getStringCellValue();
                fOp = feeRepository.findByStudentPersonIdAndDay(personId, day);  //查询是否存在记录
                if (fOp.isEmpty()) {
                    f = new Fee();
                    f.setDay(day);
                    f.setStudent(student);  //不存在 添加
                } else {
                    f = fOp.get();  //存在 更新
                }
                if (money != null && !money.isEmpty())
                    dMoney = Double.parseDouble(money);
                else
                    dMoney = 0d;
                f.setMoney(dMoney);
                feeRepository.save(f);
            }
            workbook.close();  //关闭Excl输入流
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
            return "上传错误！";
        }

    }

    public DataResponse importFeeData(@RequestBody byte[] barr,
                                      String personIdStr
                                      ) {
        Integer personId =  Integer.parseInt(personIdStr);
        String msg = importFeeData(personId,new ByteArrayInputStream(barr));
        if(msg == null)
            return CommonMethod.getReturnMessageOK();
        else
            return CommonMethod.getReturnMessageError(msg);
    }

    public ResponseEntity<StreamingResponseBody> getStudentListExcl( DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        List<Map<String,Object>> list = getStudentMapList(numName);
        Integer[] widths = {8, 20, 10, 15, 15, 15, 25, 10, 15, 30, 20, 30};
        int i, j, k;
        String[] titles = {"序号", "学号", "姓名", "学院", "专业", "班级", "证件号码", "性别", "出生日期", "邮箱", "电话", "地址"};
        String outPutSheetName = "student.xlsx";
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFCellStyle styleTitle = CommonMethod.createCellStyle(wb, 20);
        XSSFSheet sheet = wb.createSheet(outPutSheetName);
        for (j = 0; j < widths.length; j++) {
            sheet.setColumnWidth(j, widths[j] * 256);
        }
        //合并第一行
        XSSFCellStyle style = CommonMethod.createCellStyle(wb, 11);
        XSSFRow row = null;
        XSSFCell[] cell = new XSSFCell[widths.length];
        row = sheet.createRow((int) 0);
        for (j = 0; j < widths.length; j++) {
            cell[j] = row.createCell(j);
            cell[j].setCellStyle(style);
            cell[j].setCellValue(titles[j]);
            cell[j].getCellStyle();
        }
        Map<String,Object> m;
        if (list != null && !list.isEmpty()) {
            for (i = 0; i < list.size(); i++) {
                row = sheet.createRow(i + 1);
                for (j = 0; j < widths.length; j++) {
                    cell[j] = row.createCell(j);
                    cell[j].setCellStyle(style);
                }
                m = list.get(i);
                cell[0].setCellValue((i + 1) + "");
                cell[1].setCellValue(CommonMethod.getString(m, "num"));
                cell[2].setCellValue(CommonMethod.getString(m, "name"));
                cell[3].setCellValue(CommonMethod.getString(m, "dept"));
                cell[4].setCellValue(CommonMethod.getString(m, "major"));
                cell[5].setCellValue(CommonMethod.getString(m, "className"));
                cell[6].setCellValue(CommonMethod.getString(m, "card"));
                cell[7].setCellValue(CommonMethod.getString(m, "genderName"));
                cell[8].setCellValue(CommonMethod.getString(m, "birthday"));
                cell[9].setCellValue(CommonMethod.getString(m, "email"));
                cell[10].setCellValue(CommonMethod.getString(m, "phone"));
                cell[11].setCellValue(CommonMethod.getString(m, "address"));
            }
        }
        try {
            StreamingResponseBody stream = wb::write;
            return ResponseEntity.ok()
                    .contentType(CommonMethod.exelType)
                    .body(stream);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    public DataResponse getStudentPageData(DataRequest dataRequest) {
        String numName = dataRequest.getString("numName");
        Integer cPage = dataRequest.getCurrentPage();
        int dataTotal = 0;
        int size = 40;
        List<Map<String,Object>> dataList = new ArrayList<>();
        Page<Student> page = null;
        Pageable pageable = PageRequest.of(cPage, size);
        page = studentRepository.findStudentPageByNumName(numName, pageable);
        Map<String,Object> m;
        if (page != null) {
            dataTotal = (int) page.getTotalElements();
            List<Student> list = page.getContent();
            if (!list.isEmpty()) {
                for (Student student : list) {
                    m = getMapFromStudent(student);
                    dataList.add(m);
                }
            }
        }
        Map<String,Object> data = new HashMap<>();
        data.put("dataTotal", dataTotal);
        data.put("pageSize", size);
        data.put("dataList", dataList);
        return CommonMethod.getReturnData(data);
    }



    /*
        FamilyMember
     */
    public DataResponse getFamilyMemberList(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        List<FamilyMember> fList = familyMemberRepository.findByStudentPersonId(personId);
        List<Map<String,Object>> dataList = new ArrayList<>();
        Map<String,Object> m;
        if (fList != null) {
            for (FamilyMember f : fList) {
                m = new HashMap<>();
                m.put("memberId", f.getMemberId());
                m.put("personId", f.getStudent().getPersonId());
                m.put("relation", f.getRelation());
                m.put("name", f.getName());
                m.put("gender", f.getGender());
                m.put("age", f.getAge()+"");
                m.put("unit", f.getUnit());
                dataList.add(m);
            }
        }
        return CommonMethod.getReturnData(dataList);
    }

    public DataResponse familyMemberSave(DataRequest dataRequest) {
        Map<String,Object> form = dataRequest.getMap("form");
        Integer personId = CommonMethod.getInteger(form,"personId");
        Integer memberId = CommonMethod.getInteger(form,"memberId");
        Optional<FamilyMember> op;
        FamilyMember f = null;
        if(memberId != null) {
            op = familyMemberRepository.findById(memberId);
            if(op.isPresent()) {
                f = op.get();
            }
        }
        if(f== null) {
            f = new FamilyMember();
            assert personId != null;
            f.setStudent(studentRepository.findById(personId).get());
        }
        f.setRelation(CommonMethod.getString(form,"relation"));
        f.setName(CommonMethod.getString(form,"name"));
        f.setGender(CommonMethod.getString(form,"gender"));
        f.setAge(CommonMethod.getInteger(form,"age"));
        f.setUnit(CommonMethod.getString(form,"unit"));
        familyMemberRepository.save(f);
        return CommonMethod.getReturnMessageOK();
    }

    public DataResponse familyMemberDelete(DataRequest dataRequest) {
        Integer memberId = dataRequest.getInteger("memberId");
        Optional<FamilyMember> op;
        op = familyMemberRepository.findById(memberId);
        op.ifPresent(familyMemberRepository::delete);
        return CommonMethod.getReturnMessageOK();
    }


    public DataResponse importFeeDataWeb(Map<String,Object> request,MultipartFile file) {
        Integer personId = CommonMethod.getInteger(request, "personId");
        try {
            String msg= importFeeData(personId,file.getInputStream());
            if(msg == null)
                return CommonMethod.getReturnMessageOK();
            else
                return CommonMethod.getReturnMessageError(msg);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return CommonMethod.getReturnMessageError("上传错误！");
    }

    public DataResponse getStudentIntroduceData(DataRequest dataRequest) {
        Integer personId = dataRequest.getInteger("personId");
        Optional<Student> sOp;
        if(personId == null || personId <= 0) {
            String username = CommonMethod.getUsername();
            sOp = studentRepository.findByPersonNum(username);  // 查询获得 Student对象
        }else {
            sOp = studentRepository.findById(personId);  // 根据personId查询获得 Student对象
        }
        if (sOp.isEmpty())
            return CommonMethod.getReturnMessageError("学生不存在！");
        Student s = sOp.get();
        Map<String,Object> info = getMapFromStudent(s);  // 查询学生信息Map对象
        List<Score> sList = scoreRepository.findByStudentPersonId(s.getPersonId()); //获得学生成绩对象集合
        Map<String,Object> data = new HashMap<>();
        data.put("info", info);
        data.put("scoreList", getStudentScoreList(sList));
        data.put("markList", getStudentMarkList(sList));
        data.put("feeList", getStudentFeeList(s.getPersonId()));
        return CommonMethod.getReturnData(data);//将前端所需数据保留Map对象里，返还前端
    }
}
