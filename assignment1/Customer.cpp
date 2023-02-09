//
// Created by zohar on 18/11/2021.
//


#include "../include/Customer.h"
#include <algorithm>
#include <iostream>

//----------------Customer----------------//
Customer:: Customer(std::string c_name, int c_id): name(c_name), id(c_id){}  //constructor

std::string Customer:: getName() const{
    return name;
}
int Customer:: getId() const{
    return id;
}

//----------------SweatyCustomer----------------//
SweatyCustomer::SweatyCustomer(std::string name, int id) : Customer(name, id){}

std::vector<int> SweatyCustomer::order(const std::vector<Workout> &workout_options) {
    std::vector<int> customerOrder;
    for (Workout work: workout_options) {
        if (work.getType() == CARDIO)
            customerOrder.push_back(work.getId());
    }
    return customerOrder;
}

std::string SweatyCustomer::toString() const {
    return "swt";
}
Customer *SweatyCustomer::clone() {
    return new SweatyCustomer(*this);
}

//----------------CheapCustomer----------------//
CheapCustomer::CheapCustomer(std::string name, int id) : Customer(name, id) {}

std::vector<int> CheapCustomer::order(const std::vector<Workout> &workout_options) {
    std::vector<int> customerOrder;
    int cheapPrice = workout_options.begin()->getPrice();
    int cheapId = workout_options.begin()->getId();
    for (Workout work: workout_options) {
        if (work.getPrice() == cheapPrice && work.getId()<cheapId)
            cheapId = work.getId();
        else if (work.getPrice() < cheapPrice) {
            cheapId = work.getId();
            cheapPrice = work.getPrice();
        }
    }
    customerOrder.push_back(cheapId);
    return customerOrder;
}

std::string CheapCustomer::toString() const {
    return "chp";
}

Customer *CheapCustomer::clone() {
    return new CheapCustomer(*this);
}

//----------------HeavyMuscleCustomer----------------//
HeavyMuscleCustomer::HeavyMuscleCustomer(std::string name, int id) : Customer(name, id) {}

std::vector<int> HeavyMuscleCustomer::order(const std::vector<Workout> &workout_options) {
    std::vector<int> customerOrder;
    for(auto work: workout_options){
        size_t i=0;
        if(work.getType() == ANAEROBIC){
            if(customerOrder.empty())
                customerOrder.push_back(work.getId());
            else {
                while(i<customerOrder.size() && workout_options[customerOrder[i]].getPrice() > work.getPrice())
                    i++;
                if(workout_options[customerOrder[i]].getPrice() == work.getPrice() && work.getId() < workout_options[customerOrder[i]].getId())
                        customerOrder.insert(customerOrder.begin()+i, work.getId());
                else
                    customerOrder.insert(customerOrder.begin()+i, work.getId());
            }
        }
    }
    return customerOrder;
}
std::string HeavyMuscleCustomer::toString() const {
    return "mcl";
}

Customer *HeavyMuscleCustomer::clone() {
    return new HeavyMuscleCustomer(*this);
}

//----------------FullBodyCustomer----------------//
FullBodyCustomer::FullBodyCustomer(std::string name, int id) : Customer(name, id) {}

std::vector<int> FullBodyCustomer::order(const std::vector<Workout> &workout_options) {
    std::vector<int> customerOrder;
    int cheapCardio = -1;
    int cardioId = -1;
    int expensiveMixed = -1;
    int mixedId = -1;
    int cheapAnerobic = -1;
    int anerobicId = -1;
    for (Workout work: workout_options) {
        if (work.getType() == CARDIO) {
            if (cheapCardio == -1) {
                cheapCardio = work.getPrice();
                cardioId = work.getId();
            }
            else if(work.getPrice() == cheapCardio && work.getId() < cardioId) {
                    cardioId = work.getId();
                }
                else if (work.getPrice() < cheapCardio) { {
                    cheapCardio = work.getPrice();
                    cardioId = work.getId();
                }
            }
        }
        else if (work.getType() == MIXED) {
            if (expensiveMixed == -1) {
                expensiveMixed = work.getPrice();
                mixedId = work.getId();
            }
            if (work.getPrice() == expensiveMixed && work.getId() < mixedId)
                mixedId = work.getId();
            else if(work.getPrice() > expensiveMixed ){
                expensiveMixed = work.getPrice();
                mixedId = work.getId();
            }
        }
        else {
            if (cheapAnerobic == -1) {
                cheapAnerobic = work.getPrice();
                anerobicId = work.getId();
            }
            if (work.getPrice() == cheapAnerobic && work.getId() < anerobicId)
                anerobicId = work.getId();
            else if(work.getPrice() < cheapAnerobic){
                anerobicId = work.getId();
                cheapAnerobic = work.getPrice();
            }
        }
    }
    if (cardioId != -1)
        customerOrder.push_back(cardioId);
    if (mixedId != -1)
        customerOrder.push_back(mixedId);
    if(anerobicId != -1)
        customerOrder.push_back(anerobicId);
    return customerOrder;
}
    std::string FullBodyCustomer::toString() const {
        return "fbd";
    }
Customer *FullBodyCustomer::clone() {
    return new FullBodyCustomer(*this);
}
