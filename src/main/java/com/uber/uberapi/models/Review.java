package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "review")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Review extends Auditable{

    private Integer ratingOutOfFive;

    private String note;
}
