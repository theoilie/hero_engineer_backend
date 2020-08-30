package com.heroengineer.hero_engineer_backend.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entry of a single time XP was given to a student.
 * Each entry requires a reason for the change in XP in order to track for accreditation and other purposes.
 */
// TODO: Use timestamps or at least dates for more granular/transparent tracking
@Data
@AllArgsConstructor
@NoArgsConstructor
public class XpEntry {

    public int xpChangeAmount;
    public String reasonForChange;

}
