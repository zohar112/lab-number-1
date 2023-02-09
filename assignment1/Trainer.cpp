
#include "iostream"
#include "../include/Studio.h"
#include "vector"

//------------------RULE OF 5------------------//
//constructor
Trainer::Trainer(int t_capacity):capacity(t_capacity),salary(0),id(0), open(false), customersList(), orderList()  {}

//destructor
Trainer::~Trainer() {
    clear();
}
//move constructor
Trainer::Trainer(const Trainer &&other) :capacity(other.capacity), salary(other.salary),id(other.id), open(other.open), customersList(), orderList(){
   customersList = other.customersList;
    for (auto cust : other.customersList) {
        if(cust != nullptr) {
            delete cust;
            cust = nullptr;
        }
    }
    orderList=std::vector<OrderPair>(other.orderList);
}

//copy constructor
Trainer::Trainer(const Trainer &other): capacity(other.capacity), salary(other.salary), id(other.id), open(other.open),  customersList(), orderList() {
    for (auto cust : other.customersList) {
        customersList.push_back(cust->clone());
    }
    orderList=std::vector<OrderPair>(other.orderList);
}
//copy assigment
Trainer &Trainer::operator=(const Trainer &other) {
    if (this != &other) {
        clear();
        open = other.open;
        id=other.id;
        capacity = other.capacity;
        orderList=std::vector<OrderPair>(other.orderList);
        for(auto &customer : other.customersList)
            customersList.push_back(customer->clone()); //deep copy with help function
    }
    return *this;
}
//move assigment
Trainer &Trainer::operator=(Trainer &&other) {
    if (this != &other) {
        clear();
        open = other.open;
        id=other.id;
        capacity = other.capacity;
        orderList= std::vector<OrderPair>(other.orderList);
        customersList = std::move(other.customersList);
    }
    return *this;
}

void Trainer::clear() {
    for (auto cust : customersList) {
        if(cust != nullptr) {
            delete cust;
        }
    }
    customersList.clear();
    orderList.clear();
    salary=0;
}
//--------------------------------//

int Trainer::getCapacity() const {
    return capacity;
}

bool Trainer::isOpen() {
    return open;
}

void Trainer::openTrainer() {
    open = true;
}

void Trainer::closeTrainer() {
    open = false;
    for(auto customer : customersList){
        delete customer;
    }
    customersList.clear();
    orderList.clear();
}

Customer *Trainer::getCustomer(int id) {
    for(unsigned int i = 0; i< customersList.size(); i++) {
        if (customersList[i]->getId() == id)
            return customersList[i];
    }
    return nullptr;
}

void Trainer::removeCustomer(int id) {
    for(unsigned int j=0; j<customersList.size(); j++) {
        if (customersList[j]->getId() == id) {
            customersList.erase(customersList.begin() + j);
        }
    }
}

int Trainer::getSalary() {
    return salary;
}

void Trainer::updateSalary(bool moreOrless, int sum) {
    if(moreOrless)
        salary = salary+ sum;
    else salary = salary-sum;
}

int Trainer::getId() {
    return id;
}

std::vector<Customer *> &Trainer::getCustomers() {
    return customersList;
}

std::vector<OrderPair> &Trainer::getOrders() {
    return orderList;
}

void Trainer::setId(int id) {
    this->id = id;
}

void Trainer::order(const int customer_id, const std::vector<int> workout_ids, const std::vector<Workout> &workout_options) {
    for(auto pairs: orderList) {
        if(pairs.first==customer_id)
            return;
    }
    for(const auto &workId: workout_ids){
        Workout w = workout_options[workId];
        orderList.push_back(std::make_pair(customer_id,w));
        salary += w.getPrice();
    }
}

void Trainer::moveCustomer(Customer* customer, Trainer* dstTrainer) {
    dstTrainer->addCustomer(customer);
    int id = customer->getId();
    this->removeCustomer(id);
    std::vector<OrderPair> tmpCustOrds;
    for (auto pair: orderList) {
        tmpCustOrds.push_back(pair);
        if (pair.first == id)
            dstTrainer->orderList.push_back(pair);
    }
    orderList.clear();
    for(auto pair: tmpCustOrds){
        if(pair.first!=id)
            orderList.push_back(pair);
    }
}
void Trainer::addCustomer(Customer *customer) {
    size_t thisCap = this->getCapacity();
    if(this->isOpen() && this->customersList.size()<thisCap) {
        customersList.push_back(customer);
    }
}
Trainer *Trainer::clone() {
   return new Trainer(*this);
}
