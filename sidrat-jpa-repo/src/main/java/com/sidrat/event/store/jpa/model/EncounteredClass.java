package com.sidrat.event.store.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import com.sidrat.event.store.jpa.Named;

@Entity
@Table(indexes = { @Index(columnList = "partition,name,id") }) // tediously copied on all entities
public class EncounteredClass extends SidratValueObject implements Named {
}
