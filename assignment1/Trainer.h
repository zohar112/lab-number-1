#ifndef TRAINER_H_
#define TRAINER_H_

#include <vector>
#include "Customer.h"
#include "Workout.h"

typedef std::pair<int, Workout> OrderPair;

class Trainer{
public:
    Trainer(int t_capacity);

    //rule of 5
    virtual ~ Trainer();  //destructor
    Trainer(const Trainer& other); //copy
    Trainer(const Trainer&& other); //move
    Trainer& operator=(const Trainer& other); //assignment
    Trainer& operator=(Trainer&& other);//move assignment

    int getCapacity() const;
    void addCustomer(Customer* customer);
    void removeCustomer(int id);
    Customer* getCustomer(int id);
    std::vector<Customer*>& getCustomers();
    std::vector<OrderPair>& getOrders();
    void order(const int customer_id, const std::vector<int> workout_ids, const std::vector<Workout>& workout_options);
    void openTrainer();
    void closeTrainer();
    int getSalary();
    bool isOpen();
    void updateSalary(bool, int);
    void setId(int);
    int getId();
    void clear();
    void moveCustomer(Customer* customer, Trainer* dstTrainer);
    Trainer* clone();
private:
    int capacity;
    int salary;
    int id;
    bool open;
    std::vector<Customer*> customersList;
    std::vector<OrderPair> orderList; //A list of pairs for each order for the trainer - (customer_id, Workout)

};


#endif