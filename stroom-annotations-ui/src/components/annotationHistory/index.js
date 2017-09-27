import React, { Component } from 'react'
import { connect } from 'react-redux'
import moment from 'moment'

import {
  Step,
  Stepper,
  StepButton,
  StepContent,
} from 'material-ui/Stepper'

import RaisedButton from 'material-ui/RaisedButton'
import FlatButton from 'material-ui/FlatButton'

import { fetchAnnotationHistory } from '../../actions/fetchAnnotationHistory'

export class AnnotationHistory extends Component {
    state = {
        stepIndex: 0,
    };

    handleRefresh() {
        this.props.fetchAnnotationHistory(this.props.annotationId)
    }

    handleNext = () => {
        const {stepIndex} = this.state;
        if (stepIndex < this.props.annotationHistory.history.length) {
            this.setState({stepIndex: stepIndex + 1});
        }
    };

    handlePrev = () => {
        const {stepIndex} = this.state;
        if (stepIndex > 0) {
          this.setState({stepIndex: stepIndex - 1});
        }
    };

    renderStepActions(stepIndex) {
        return (
            <div style={{margin: '12px 0'}}>
                <RaisedButton
                    label={stepIndex === (this.props.annotationHistory.history.length-1) ? 'Finish' : 'Next'}
                    disableTouchRipple={true}
                    disableFocusRipple={true}
                    primary={true}
                    onClick={this.handleNext}
                    style={{marginRight: 12}}
                    />
                    {stepIndex > 0 && (
                        <FlatButton
                            label="Back"
                            disableTouchRipple={true}
                            disableFocusRipple={true}
                            onClick={this.handlePrev}
                            />
                    )}
            </div>
        );
    }

    renderHistory() {
        return this.props.annotationHistory.history.slice().reverse().map((h, i) => (
            <Step key={i}>
                <StepButton onClick={() => this.setState({stepIndex: i})}>
                    {h.operation} by {h.annotation.updatedBy} on {moment(h.annotation.lastUpdated).fromNow()}
                </StepButton>
                <StepContent>
                    <p>
                    {h.annotation.content}
                    </p>
                    {this.renderStepActions(i)}
                </StepContent>
            </Step>

        ))
    }

  render() {
    const {stepIndex} = this.state;

    if (this.props.annotationHistory.history.length > 0) {
        return (
            <div style={{maxWidth: 600, maxHeight: 400}}>
                <h1>History</h1>
                <RaisedButton
                    label='Refresh'
                    onClick={this.handleRefresh.bind(this)}
                    primary={true}
                />
                <Stepper
                    activeStep={stepIndex}
                    linear={false}
                    orientation="vertical"
                    >
                    {this.renderHistory()}
                </Stepper>
            </div>
        );
    } else {
        return <div />
    }

  }
}

export default connect(
  (state) => ({
     annotationId: state.annotation.annotationId,
     annotationHistory: state.annotationHistory
  }),
  {
    fetchAnnotationHistory
  }
)(AnnotationHistory)
