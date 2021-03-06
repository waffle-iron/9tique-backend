package kr.co.mash_up.nine_tique.repository;

import kr.co.mash_up.nine_tique.domain.Zzim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "zzimRepository")
public interface ZzimRepository extends JpaRepository<Zzim, Long>, ZzimRepositoryCustom {
}
