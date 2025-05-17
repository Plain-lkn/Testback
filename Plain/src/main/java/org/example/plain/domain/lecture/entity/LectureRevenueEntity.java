package org.example.plain.domain.lecture.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.plain.domain.user.entity.User;


@Entity
@Table(name = "lecture_revenue")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureRevenueEntity {

    @Id
    @Column(name = "order_id")
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private LectureEntity lecture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "price")
    private Integer price;
} 