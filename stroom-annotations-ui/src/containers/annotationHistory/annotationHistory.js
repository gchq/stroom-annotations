import React, { Component } from 'react'
import PropTypes from 'prop-types'
import moment from 'moment'

import { NavLink } from 'react-router-dom'

import Paper from 'material-ui/Paper'
import KeyboardArrowRight from 'material-ui-icons/KeyboardArrowRight'
import { Toolbar, ToolbarGroup, ToolbarTitle } from 'material-ui/Toolbar'

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

    toggleStep(i) {
        if (this.state.stepIndex === i) {
            this.setState({ stepIndex: -1 })
        } else {
            this.setState({ stepIndex: i })
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
                        <TextField value={h.content} onChange={() => { }}
                            hintText="Content was empty"
                            floatingLabelText="Content"
                            multiLine={true}
                            rows={1}
                            rowsMax={4}
                        />
                        <TextField value={h.assignTo} onChange={() => { }}
                            hintText="Wasn't assigned to anyone"
                            floatingLabelText="Assign To"
                        />
                        <TextField value={h.status} onChange={() => { }}
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

        const { stepIndex } = this.state;

        let editAnnotationUrl = (this.props.allowNavigation) ?
            `/singleWithNav/${this.props.indexUuid}/${this.props.annotationId}`
            :
            `/single/${this.props.indexUuid}/${this.props.annotationId}`

        let rootNav = (this.props.allowNavigation) ?
            <NavLink to={`/${this.props.indexUuid}`}>
                <ToolbarTitle text='Annotations' className='toolbar-title-small' />
            </NavLink>
            :
            <ToolbarTitle text='Annotations' className='toolbar-title-small' />

        return (
            <Paper className='ManageAnnotations-main' zDepth={0}>
                <Toolbar className='toolbar-small'>
                    <ToolbarGroup>
                        {rootNav}
                        <KeyboardArrowRight />
                        <NavLink to={editAnnotationUrl}>
                            <ToolbarTitle text='Edit Annotation' className='toolbar-title-small' />
                        </NavLink>
                        <KeyboardArrowRight />
                        <ToolbarTitle text='History' className='toolbar-title-small' />
                    </ToolbarGroup>
                </Toolbar>

                <div className='edit-annotation'>
                    <Stepper
                        activeStep={stepIndex}
                        linear={false}
                        orientation="vertical"
                    >
                        {this.renderHistory()}
                    </Stepper>
                </div>
                <SnackbarDisplay />
            </Paper>
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