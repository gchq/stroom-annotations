import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'

import {
  Step,
  Stepper,
  StepButton,
  StepContent,
} from 'material-ui/Stepper'
import TextField from 'material-ui/TextField'

class History extends Component {
    state = {
        stepIndex: -1,
    };

    componentDidMount() {
        if (this.props.annotationId) {
            this.props.fetchAnnotationHistory(this.props.indexUuid, this.props.annotationId)
        }
    }

    toggleStep(i) {
        if (this.state.stepIndex === i) {
            this.setState({stepIndex: -1})
        } else {
            this.setState({stepIndex: i})
        }
    }

    renderHistory() {
        const stepContentStyle = {
            display: "flex",
            flexDirection: "column"
        }

        return this.props.annotationHistory.slice().reverse().map((h, i) => (
            <Step key={i}>
                <StepButton onClick={() => this.toggleStep(i)}>
                    {h.operation} by {h.annotation.updatedBy} on {moment(h.annotation.lastUpdated).fromNow()}
                </StepButton>
                <StepContent>
                    <div style={stepContentStyle}>
                        <TextField value={h.annotation.content} onChange={() => {}}
                            hintText="Content was empty"
                            floatingLabelText="Content"
                            multiLine={true}
                            rows={1}
                            rowsMax={4}
                        />
                        <TextField value={h.annotation.assignTo} onChange={() => {}}
                            hintText="Wasn't assigned to anyone"
                            floatingLabelText="Assign To"
                        />
                        <TextField value={h.annotation.status} onChange={() => {}}
                            hintText="No Status Found"
                            floatingLabelText="Status"
                        />
                    </div>
                </StepContent>
            </Step>

        ))
    }

  render() {
    const {stepIndex} = this.state;

    if (this.props.annotationHistory.length > 0) {
        return (
            <div>
                <h1>History</h1>
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

History.propTypes = {
    // set from routing
    annotationId: PropTypes.string.isRequired,

    annotationHistory: PropTypes.array.isRequired
}

export default History