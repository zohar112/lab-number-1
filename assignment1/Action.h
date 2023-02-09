#ifndef ACTION_H_
#define ACTION_H_

#include <string>
#include <iostream>
#include "Customer.h"

enum ActionStatus{
    COMPLETED, ERROR
};
//Forward declaration
class Studio;

class BaseAction{
public:
    BaseAction();
    ActionStatus getStatus() const;
    virtual void act(Studio& studio)=0;
    virtual std::string toString() const=0;
    virtual ~BaseAction()= default;
    virtual BaseAction* clone()=0;
    std::string printError();
protected:
    void complete();
    void error(std::string errorMsg);
    std::string getErrorMsg() const;
private:
    std::string errorMsg;
    ActionStatus status;
};

class OpenTrainer : public BaseAction {
public:
    OpenTrainer(int id, std::vector<Customer *> &customersList);
    void act(Studio &studio);
    std::string toString() const;
    virtual ~OpenTrainer()=default;
    std::string string="";
    virtual OpenTrainer* clone();
private:
	const int trainerId;
	std::vector<Customer *> customers;
};


class Order : public BaseAction {
public:
    Order(int id);
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ Order()=default;
    virtual Order* clone();
private:
    const int trainerId;
};


class MoveCustomer : public BaseAction {
public:
    MoveCustomer(int src, int dst, int customerId);
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ MoveCustomer()=default;
    virtual MoveCustomer* clone();
private:
    const int srcTrainer;
    const int dstTrainer;
    const int id;
};

class Close : public BaseAction {
public:
    Close(int id);
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ Close()= default;
    virtual Close* clone();
private:
    const int trainerId;
};

class CloseAll : public BaseAction {
public:
    CloseAll();
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ CloseAll()=default;
    virtual CloseAll* clone();
private:
};

class PrintWorkoutOptions : public BaseAction {
public:
    PrintWorkoutOptions();
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ PrintWorkoutOptions() =default;
    virtual PrintWorkoutOptions* clone();
private:
};


class PrintTrainerStatus : public BaseAction {
public:
    PrintTrainerStatus(int id);
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ PrintTrainerStatus()=default;
    virtual PrintTrainerStatus* clone();
private:
    const int trainerId;
};

class PrintActionsLog : public BaseAction {
public:
    PrintActionsLog();
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ PrintActionsLog()=default;
    virtual PrintActionsLog* clone();
private:
};

class BackupStudio : public BaseAction {
public:
    BackupStudio();
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ BackupStudio()=default;
    virtual BackupStudio* clone();
private:
};

class RestoreStudio : public BaseAction {
public:
    RestoreStudio();
    void act(Studio &studio);
    std::string toString() const;
    virtual ~ RestoreStudio()=default;
    virtual RestoreStudio* clone();
};

#endif