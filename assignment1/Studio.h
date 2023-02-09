#ifndef STUDIO_H_
#define STUDIO_H_

#include <vector>
#include <string>
#include "Workout.h"
#include "Trainer.h"
#include "Action.h"


class Studio{		
public:
	Studio();
    Studio(const std::string &configFilePath);

    //rule of 5
    Studio(Studio const &other); //copy constructor
    Studio(Studio &&other); //move constructor
    Studio &operator=(const Studio &other); //assignment operator
    Studio &operator=(Studio &&other);; //move assigment operator
    ~Studio(); //destructor
    void clear();

    void start();
    int getNumOfTrainers() const;
    Trainer* getTrainer(int tid);
	const std::vector<BaseAction*>& getActionsLog() const; // Return a reference to the history of actions
    std::vector<Workout>& getWorkoutOptions();
    std::vector<Trainer*> getAllTrainers();
    void setOpen(bool);
private:
    bool open;
    std::vector<Trainer*> trainers;
    std::vector<Workout> workout_options;
    std::vector<BaseAction*> actionsLog;
    int customerId;
};

#endif