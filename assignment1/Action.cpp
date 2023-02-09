//
// Created by zohar on 18/11/2021.
//

#include "../include/Action.h"
#include "../include/Trainer.h"
#include "../include/Studio.h"
#include "iostream"
#include <algorithm>
extern Studio* backup;

//----------------BaseAction----------------//

void BaseAction::complete() {
    status= COMPLETED;
}
void BaseAction::error(std::string errorMsg) {
    status= ERROR;
    this->errorMsg = errorMsg;
    std::cout<<getErrorMsg()<<std::endl;
}
std::string BaseAction::getErrorMsg() const {
    return errorMsg;
}
ActionStatus BaseAction::getStatus() const {
    return status;
}
BaseAction::BaseAction(): errorMsg(""),status(ERROR) {}

std::string BaseAction::printError() {
    return getErrorMsg();
}

//----------------OpenTrainer----------------//
OpenTrainer:: OpenTrainer(int id, std::vector<Customer *> &customersList): BaseAction(), trainerId(id), customers(customersList){
}
void OpenTrainer:: act(Studio &studio){
    Trainer* thisTrainer = studio.getTrainer(trainerId);
    size_t id = trainerId;
    string = "open "+std::to_string(trainerId);
    if(nullptr == thisTrainer || id > studio.getAllTrainers().size() || thisTrainer->isOpen()) {
        for (auto cust: customers) {
            delete cust;
        }
        customers.clear();
        error("Workout session does not exist or is already open");
    }
    else{
        thisTrainer->openTrainer();
        for (Customer * customer: customers) {
            thisTrainer->addCustomer(customer);
            string = string + " "+ customer->getName() + "," + customer->toString();
        }
        complete();
    }
}
std::string OpenTrainer::toString() const {
    return string;
}
OpenTrainer *OpenTrainer::clone() {
    return new OpenTrainer(*this);
}

//----------------Order----------------//
Order::Order(int id): trainerId(id){}

void Order::act(Studio &studio) {
    Trainer *thisTrainer = studio.getTrainer(trainerId);
    if(thisTrainer == nullptr || !thisTrainer->isOpen())
        error("Trainer does not exist or is not open");
    else {
        std::vector<Workout> thisWorkout = studio.getWorkoutOptions();
        for (auto custom : thisTrainer->getCustomers()) { //for each customer
            std::vector<int> customOrder = custom->order(studio.getWorkoutOptions());
            thisTrainer->order(custom->getId(),customOrder, thisWorkout);
        }
        for(auto p : thisTrainer->getOrders()) {
            Customer *c = thisTrainer->getCustomer(p.first);
            std::cout << c->getName()<< " Is Doing "<< p.second.getName()<<std::endl;
        }
        complete();
    }
}
std::string Order::toString() const {
    return "order "+std::to_string(trainerId);
}

Order *Order::clone() {
    return new Order(*this);
}

//----------------MoveCustomer----------------//
MoveCustomer::MoveCustomer(int src, int dst, int customerId): srcTrainer(src), dstTrainer(dst), id(customerId) {}

void MoveCustomer::act(Studio &studio) {
    Trainer* srctrain = studio.getTrainer(srcTrainer);
    Trainer* dstrain = studio.getTrainer(dstTrainer);
    Customer* moved = studio.getTrainer(srcTrainer)->getCustomer(id);
    size_t cap = dstrain->getCapacity();
    if(srctrain == nullptr || dstrain == nullptr ||!srctrain->isOpen() || !dstrain->isOpen() || srctrain->getCustomer(id)==nullptr || dstrain->getCustomers().size()==cap)
        error("Cannot move customer");
    else {
        int salaryChange = 0;
        std::vector<Workout> studioWork = studio.getWorkoutOptions();
        srctrain->moveCustomer(moved, dstrain);
        for(auto CustOrd : moved->order(studioWork)){ //update salaries
            salaryChange += studioWork[CustOrd].getPrice();
        }
        srctrain->updateSalary(false, salaryChange);
        dstrain->updateSalary(true, salaryChange);
        if(srctrain->getCustomers().size()==0){//to close the origin trainer if left with 0 zero customers
            srctrain->closeTrainer();
            std:: cout <<"trainer " << srctrain->getId() <<" closed. Salary " << srctrain->getSalary() <<"Nis."<<std::endl;
        }
        complete();
    }
}
std::string MoveCustomer::toString() const {
    return "move "+std::to_string(dstTrainer)+" "+std::to_string(srcTrainer)+" "+std::to_string(id);
}

MoveCustomer *MoveCustomer::clone() {
    return new MoveCustomer(*this);
}

//----------------Close----------------//
Close:: Close(int id): trainerId(id){}

void Close:: act(Studio &studio) {
    Trainer* thisTrainer = studio.getTrainer(trainerId);
    if(thisTrainer!=nullptr && thisTrainer->isOpen()) {
        thisTrainer->closeTrainer();
        std:: cout <<"trainer " << trainerId <<" closed. Salary " << thisTrainer->getSalary() <<"Nis."<<std::endl;
        complete();
    }
    else
        error("Trainer does not exist or is not open");
}
std::string Close::toString() const {
    return "close "+std::to_string(trainerId);
}

Close *Close::clone() {
    return new Close(*this);
}

//----------------CloseAll----------------//
CloseAll:: CloseAll() {}

void CloseAll:: act(Studio &studio) {
    std::vector<Trainer*> trainers = studio.getAllTrainers();
    std::sort(trainers.begin(), trainers.end());
    for (auto trainer : trainers) { //Go over all the trainers and close them
        if(trainer->isOpen()) {
            Close close = Close(trainer->getId());
            close.act(studio);
        }
    }
    studio.setOpen(false);
    std::cout << "studio is now closed!";
    complete();
}

std::string CloseAll::toString() const {
    return "closeall";
}

CloseAll *CloseAll::clone() {
    return new CloseAll(*this);
}

//----------------PrintWorkoutOptions----------------//
PrintWorkoutOptions::PrintWorkoutOptions() {}

void PrintWorkoutOptions::act(Studio &studio) {
    for(auto work: studio.getWorkoutOptions()) {
        std::cout << work.getName() << ", " << work.toString() << ", " << work.getPrice()<<std::endl;
    }
    complete();
}
std::string PrintWorkoutOptions::toString() const {
    return "workout_options";
}

PrintWorkoutOptions *PrintWorkoutOptions::clone() {
    return new PrintWorkoutOptions(*this);
}

//----------------PrintTrainerStatus----------------//
PrintTrainerStatus::PrintTrainerStatus(int id): trainerId(id) {}

void PrintTrainerStatus::act(Studio &studio){
    Trainer* trainer = studio.getTrainer(trainerId);
    if(trainer->isOpen()) {
        std::cout << "Trainer " << trainerId << " status: open" << std::endl;
        if (trainer->isOpen()) {
            std::cout << "customers:" << std::endl;
            for (auto cust: trainer->getCustomers())
                std::cout << cust->getId() << " " << cust->getName() << std::endl;
            std::cout << "orders:" << std::endl;
            for (auto order: trainer->getOrders())
                std::cout << order.second.getName() << " " << order.second.getPrice() << "NIS " << order.first
                          << std::endl;
            std::cout << "current trainer's salary: " << trainer->getSalary() << "NIS" << std::endl;
        }
    }
    else
        std::cout<<"Trainer "<<std::to_string(trainerId)<<" status: closed"<<std::endl;
    complete();
}
std::string PrintTrainerStatus::toString() const {
    return "status " +std::to_string(trainerId);
}

PrintTrainerStatus *PrintTrainerStatus::clone() {
    return new PrintTrainerStatus(*this);
}

//----------------PrintActionsLog ----------------//
PrintActionsLog::PrintActionsLog() {}

void PrintActionsLog::act(Studio &studio) {
    for(auto action : studio.getActionsLog()) {
        if(action->getStatus() == COMPLETED)
            std::cout << action->toString() << " COMPLETED"<<std::endl;
        else std:: cout<<action->toString()<<" ERROR: "<<action->printError()<<std::endl;
    }
    complete();
}

std::string PrintActionsLog::toString() const {
    return "log ";
}

PrintActionsLog *PrintActionsLog::clone() {
    return new PrintActionsLog(*this);
}

//----------------BackupStudio----------------//
BackupStudio::BackupStudio() {}

void BackupStudio::act(Studio &studio) {
    if (backup == nullptr){
        backup = new Studio(studio);
     }
    else {
        delete backup;
        backup= nullptr;
        backup = new Studio(studio);
        }
    complete();
}
std::string BackupStudio::toString() const {
    return "backup";
}

BackupStudio *BackupStudio::clone() {
    return new BackupStudio(*this);
}

//----------------RestoreStudio----------------//
RestoreStudio::RestoreStudio() {}

void RestoreStudio::act(Studio &studio) {
    if(backup == nullptr)
        error("No backup available");
    else {
        studio = *backup;
        complete();
    }
}
std::string RestoreStudio::toString() const {
    return "restore ";
}

RestoreStudio *RestoreStudio::clone() {
    return new RestoreStudio(*this);
}
