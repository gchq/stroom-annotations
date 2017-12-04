import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'

import Paper from 'material-ui/Paper'
import RaisedButton from 'material-ui/RaisedButton'

import ApiCallSpinner from '../apiCallSpinner'
import SnackbarDisplay from '../snackbarDisplay'

import {
    Step,
    Stepper,
    StepButton,
    StepContent,
  } from 'material-ui/Stepper'
  import TextField from 'material-ui/TextField'

class AnnotationHistoryPage extends Component {
    constructor(props) {
        super(props)

        this.state = {
            stepIndex: -1,
        }
    }

    componentDidMount() {
        this.props.fetchAnnotationHistory(this.props.indexUuid, this.props.annotationId)
    }

    handleNavigateBack() {
        if (this.props.allowNavigation) {
            this.props.history.push(`/singleWithNav/${this.props.indexUuid}/${this.props.annotationId}`)
        } else {
            this.props.history.push(`/single/${this.props.indexUuid}/${this.props.annotationId}`)
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
                    {h.operation} by {h.updatedBy} on {moment(h.lastUpdated).fromNow()}
                </StepButton>
                <StepContent>
                    <div style={stepContentStyle}>
                        <TextField value={h.content} onChange={() => {}}
                            hintText="Content was empty"
                            floatingLabelText="Content"
                            multiLine={true}
                            rows={1}
                            rowsMax={4}
                        />
                        <TextField value={h.assignTo} onChange={() => {}}
                            hintText="Wasn't assigned to anyone"
                            floatingLabelText="Assign To"
                        />
                        <TextField value={h.status} onChange={() => {}}
                            hintText="No Status Found"
                            floatingLabelText="Status"
                        />
                    </div>
                </StepContent>
            </Step>

        ))
    }

    render() {
        // Indicate if the annotation information is clean

        const {stepIndex} = this.state;
    
        return (
            <div className='app'>
                <Paper className='app--body' zDepth={0}>
                    <div>
                        <h1>History</h1>

                        <RaisedButton
                                label="Back"
                                onClick={this.handleNavigateBack.bind(this)}
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
                </Paper>
                <SnackbarDisplay />
                <ApiCallSpinner />
            </div>
        );
    }
}

AnnotationHistoryPage.propTypes = {
    indexUuid: PropTypes.string.isRequired,
    annotationId: PropTypes.string.isRequired,
    allowNavigation: PropTypes.bool.isRequired,
    annotationHistory: PropTypes.array.isRequired,

    history: PropTypes.object.isRequired
}

export default AnnotationHistoryPage