package com.messmate.service;

import com.messmate.db.MemberDao;
import com.messmate.model.Member;
import com.messmate.util.AppException;
import com.messmate.util.Validators;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MemberService {
    private final MemberDao memberDao;

    public MemberService() {
        this(new MemberDao());
    }

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member add(Member member) {
        normalize(member);
        try {
            member.setId(memberDao.insert(member));
            return member;
        } catch (SQLException ex) {
            throw failure("save the member", ex);
        }
    }

    public void update(Member member) {
        if (member.getId() == null) {
            throw new AppException("Select a member to update.");
        }
        normalize(member);
        try {
            memberDao.update(member);
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Member not found")) {
                throw new AppException("This member no longer exists.");
            }
            throw failure("update the member", ex);
        }
    }

    public void delete(int id) {
        try {
            if (memberDao.delete(id) == 0) {
                throw new AppException("This member no longer exists.");
            }
        } catch (SQLException ex) {
            throw failure("delete the member", ex);
        }
    }

    public Optional<Member> get(int id) {
        try {
            return memberDao.find(id);
        } catch (SQLException ex) {
            throw failure("load the member", ex);
        }
    }

    public List<Member> search(String query) {
        try {
            return memberDao.search(query);
        } catch (SQLException ex) {
            throw failure("search members", ex);
        }
    }

    private static void normalize(Member member) {
        member.setName(Validators.requireName(member.getName()));
        member.setPhone(Validators.requirePhone(member.getPhone()));
        member.setAddress(Validators.requireAddress(member.getAddress()));
        Validators.requireDate(member.getJoiningDate(), "Joining date");
        member.setMonthlyFee(Validators.requireMoney("Monthly fee", member.getMonthlyFee()));
    }

    private static AppException failure(String action, SQLException ex) {
        ex.printStackTrace();
        return new AppException("Could not " + action + ". Please try again.");
    }
}
