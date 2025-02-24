package com.sesac.boheommong.domain.userhealth.service;

import com.sesac.boheommong.domain.user.entity.User;
import com.sesac.boheommong.domain.user.repository.UserRepository;
import com.sesac.boheommong.domain.userhealth.dto.request.UserHealthRequest;
import com.sesac.boheommong.domain.userhealth.entity.UserHealth;
import com.sesac.boheommong.domain.userhealth.enums.JobType;
import com.sesac.boheommong.domain.userhealth.repository.UserHealthRepository;
import com.sesac.boheommong.global.exception.BaseException;
import com.sesac.boheommong.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserHealthServiceImpl implements UserHealthService {

    private final UserHealthRepository userHealthRepository;
    private final UserRepository userRepository;

    /**
     * 건강정보 생성 (1:1 관계에서 이미 존재하면 예외)
     */
    @Transactional
    @Override
    public UserHealth createHealth(UserHealthRequest req) {
        // 1) userId -> User (develop 방식 예외 처리)
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) 이미 HealthInfo가 존재하는지 확인 (feature 방식 예외 처리)
        userHealthRepository.findByUser(user).ifPresent(h -> {
            throw new IllegalStateException("Health info already exists for this user.");
        });

        // 3) jobType 문자열 -> Enum (jobType 변환 유지)
        JobType jobType = JobType.valueOf(req.jobType());

        // 4) 엔티티 생성 (변수명 health 유지)
        UserHealth health = UserHealth.create(
                user,
                req.age(),
                req.gender(),
                req.height(),
                req.weight(),
                req.bloodPressureLevel(),
                req.bloodSugarLevel(),
                req.surgeryCount(),
                req.isSmoker(),
                req.isDrinker(),
                req.chronicDiseaseList(),
                jobType,
                req.hasChildren(),
                req.hasOwnHouse(),
                req.hasPet(),
                req.hasFamilyHistory()
        );

        // 5) 저장
        return userHealthRepository.save(health);
    }

    /**
     * 건강정보 업데이트
     */
    @Transactional
    @Override
    public UserHealth updateHealth(UserHealthRequest req) {
        // 1) userId -> User (develop 방식 예외 처리)
        User user = userRepository.findById(req.userId())
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) 해당 user로 HealthInfo 조회 (feature 방식 예외 처리)
        UserHealth health = userHealthRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Health info not found"));

        // 3) jobType 변환
        JobType jobType = JobType.valueOf(req.jobType());

        // 4) 업데이트
        health.updateHealth(
                req.age(),
                req.gender(),
                req.height(),
                req.weight(),
                req.bloodPressureLevel(),
                req.bloodSugarLevel(),
                req.surgeryCount(),
                req.isSmoker(),
                req.isDrinker(),
                req.chronicDiseaseList(),
                jobType,
                req.hasChildren(),
                req.hasOwnHouse(),
                req.hasPet(),
                req.hasFamilyHistory()
        );

        return health;
    }

    /**
     * 사용자 ID로 건강정보 조회
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<UserHealth> findHealthByUserId(Long userId) {
        // develop 방식 예외 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));
        return userHealthRepository.findByUser(user);
    }
}
