package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember,Integer> {
    List<FamilyMember> findByStudentPersonId(Integer personId);
}
