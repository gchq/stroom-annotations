import React, { Component } from 'react'
import { connect } from 'react-redux'

import {
    BrowserRouter,
    Route,
    Switch,
    withRouter
} from 'react-router-dom'

import './App.css'

import { AuthenticationRequest, HandleAuthenticationResponse } from 'stroom-js'

import { fetchStatusValues } from '../../actions/fetchStatusValues'

import AnnotationHistory from '../annotationHistory'
import SingleAnnotation from '../singleAnnotation'
import ManageAnnotations from '../manageAnnotations'
import NotFound from '../notFound'


class App extends Component {
    componentDidMount() {
        this.props.fetchStatusValues()
    }

    isLoggedIn() {
        return !!this.props.idToken
    }

    render() {

        return (
            <div className='App'>
                <main className='main'>
                    <div>
                        <BrowserRouter basename={'/'} />
                        <Switch>
                            <Route exact path={'/handleAuthenticationResponse'} render={() => (<HandleAuthenticationResponse
                                authenticationServiceUrl={this.props.authenticationServiceUrl}
                                authorisationServiceUrl={this.props.authorisationServiceUrl} />
                            )} />
                            <Route exact={true} path="/singleWithNav/:indexUuid/:annotationId?" component={(route) => (
                                this.isLoggedIn() ?
                                    <SingleAnnotation
                                        indexUuid={route.match.params.indexUuid}
                                        annotationId={route.match.params.annotationId}
                                        allowNavigation={true}
                                    /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )} />
                            <Route exact={true} path="/single/:indexUuid/:annotationId?" component={(route) => (
                                this.isLoggedIn() ?
                                    <SingleAnnotation
                                        indexUuid={route.match.params.indexUuid}
                                        annotationId={route.match.params.annotationId}
                                        allowNavigation={false}
                                    /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )} />
                            <Route exact={true} path="/historyWithNav/:indexUuid/:annotationId?" component={(route) => (
                                this.isLoggedIn() ?
                                    <AnnotationHistory
                                        indexUuid={route.match.params.indexUuid}
                                        annotationId={route.match.params.annotationId}
                                        allowNavigation={true}
                                    /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )} />
                            <Route exact={true} path="/history/:indexUuid/:annotationId?" render={(route) => (
                                this.isLoggedIn() ?
                                    <AnnotationHistory
                                        indexUuid={route.match.params.indexUuid}
                                        annotationId={route.match.params.annotationId}
                                        allowNavigation={false}
                                    /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )}
                            />
                            <Route exact={true} path="/:indexUuid" render={(route) => (
                                this.isLoggedIn() ?
                                    <ManageAnnotations indexUuid={route.match.params.indexUuid} /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )} />
                            <Route path="*" render={(route) => (
                                this.isLoggedIn() ?
                                    <NotFound /> :
                                    <AuthenticationRequest
                                        referrer={route.location.pathname}
                                        uiUrl={this.props.advertisedUrl}
                                        appClientId={this.props.appClientId}
                                        authenticationServiceUrl={this.props.authenticationServiceUrl} />
                            )} />
                        </Switch>
                    </div>
                </main>
            </div>
        )
    }
}

export default withRouter(connect(
    (state) => ({
        idToken: state.authentication.idToken,
        advertisedUrl: state.config.advertisedUrl,
        appClientId: state.config.appClientId,
        authenticationServiceUrl: state.config.authenticationServiceUrl,
        authorisationServiceUrl: state.config.authorisationServiceUrl
    }),
    {
        fetchStatusValues
    }
)(App));