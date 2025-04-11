package com.sds.ui.services;
import com.sds.ui.enums.CommissionStatus;
import com.sds.ui.enums.DutyStatus;
import com.sds.ui.models.Duty;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DutyService {
    private static DutyService instance;
    private final List<Duty> dutyList = new ArrayList<>();

    private DutyService() {
        // Add dummy data for now or fetch from DB/Firestore
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.APRIL, 8, 9, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        dutyList.add(new Duty("DUTY001", "123 Main Street", "Ram Verma", "9876543210",
                "Shyam Kumar", "9123456789", 1200, 300,
                CommissionStatus.RECEIVED, DutyStatus.ASSIGNED,
                calendar.getTime(), null, null));


        dutyList.add(new Duty("DUTY002", "456 Park Ave", "Sita Patel", "9834567890",
                "Mohit Raj", "8765432190", 1500, 400,
                CommissionStatus.PENDING, DutyStatus.UNASSIGNED,
                calendar.getTime(), null, null));


        dutyList.add(new Duty("DUTY003", "456 Park Ave", "Laxman Patel", "9834567890",
                "Mohit Raj", "8765432190", 1500, 400,
                CommissionStatus.PENDING, DutyStatus.IN_PROGRESS,
                calendar.getTime(), null, null));
    }

    public static synchronized DutyService getInstance() {
        if (instance == null) {
            instance = new DutyService();
        }
        return instance;
    }

    public List<Duty> getDuties() {
        return new ArrayList<>(dutyList); // return a copy to prevent external modifications
    }

    public Duty getDutyByIndex(int index) {
        return dutyList.get(index);
    }

    public void updateDuty(int index, Duty updatedDuty) {
        dutyList.set(index, updatedDuty);
    }

    public void addDuty(Duty duty) {
        dutyList.add(duty);
    }

    public void deleteDuty(int index) {
        dutyList.remove(index);
    }

    public void clearAll() {
        dutyList.clear();
    }

    public List<Duty> filterDuties(Date date, String status) {
        List<Duty> filtered = new ArrayList<>();

        for (Duty duty : getDuties()) {
            boolean dateMatch = (date == null) || isSameDay(duty.getDutyReportingTime(), date);
            boolean statusMatch = (status == null) || duty.getDutyStatus().name().equalsIgnoreCase(status);

            if (dateMatch && statusMatch) {
                filtered.add(duty);
            }
        }
        return filtered;
    }

    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
