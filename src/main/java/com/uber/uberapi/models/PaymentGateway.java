package com.uber.uberapi.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "paymentgateway")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentGateway extends Auditable {

    private String name;

    @OneToMany(mappedBy = "paymentGateway")
    Set<PaymentReceipt> receipts = new HashSet<>();
}
