/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.redirect.config;

import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.bootstrapui.elements.TextareaFormElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderHelper;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.EntityListActionsProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.util.SortableTableBuilder;
import com.foreach.across.modules.entity.views.menu.EntityAdminMenuEvent;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SimpleEntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.web.ui.DefaultViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.redirect.web.WebCmsRemoteEndpointViewElementBuilder;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.event.EventListener;
import org.springframework.format.Printer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ConditionalOnAdminUI
@Component
@RequiredArgsConstructor
class WebCmsRemoteEndpointConfiguration implements EntityConfigurer
{
	private final RemoteEndpointLabelPrinter remoteEndpointLabelPrinter;
	private final UrlsControlViewElementBuilder urlsControlViewElementBuilder;
	private final WebCmsRemoteEndpointViewElementBuilder remoteEndpointViewElementBuilder;

	@EventListener
	void hideUrlsTab( EntityAdminMenuEvent<WebCmsRemoteEndpoint> menu ) {
		menu.builder()
		    .item( "webCmsRemoteEndpoint.urls" ).disable();
	}

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsRemoteEndpoint.class )
		        .attribute( "endpointValueBuilder", remoteEndpointViewElementBuilder )
		        .entityModel( model -> model.labelPrinter( remoteEndpointLabelPrinter ) )
		        .properties(
				        props -> props
						        .property( "targetUrl" )
						        .<TextareaFormElement>viewElementPostProcessor( ViewElementMode.CONTROL, ( ctx, textbox ) -> textbox.setRows( 1 ) )
						        .and()
						        .property( "urls" )
						        .viewElementBuilder( ViewElementMode.FORM_WRITE, urlsControlViewElementBuilder )
		        )
		        .listView( lvb -> lvb.showProperties( "targetUrl" ).defaultSort( "targetUrl" ) )
		        .createFormView( fvb -> fvb.showProperties( "targetUrl" ) )
		        .updateFormView( fvb -> fvb.showProperties( "targetUrl", "urls" ).viewProcessor( new UpdateRedirectFormProcessor() ) )
		        .association(
				        ab -> ab.name( "webCmsRemoteEndpoint.urls" )
				                .associationType( EntityAssociation.Type.EMBEDDED )
				                .parentDeleteMode( EntityAssociation.ParentDeleteMode.IGNORE )
				                .createOrUpdateFormView(
						                fvb -> fvb.showProperties( "path", "httpStatus" )
						                          .properties( props -> props
								                          .property( "httpStatus" )
								                          .attribute( EntityAttributes.OPTIONS_ALLOWED_VALUES,
								                                      EnumSet.of( HttpStatus.FOUND,
								                                                  HttpStatus.MOVED_PERMANENTLY ) )
						                          )
				                )
				                .createFormView( fvb -> fvb.viewProcessor( new CreateUrlFormProcessor() ) )
				                .updateFormView( fvb -> fvb.viewProcessor( new UpdateUrlFormProcessor() ) )
				                .listView( lvb -> lvb.viewProcessor( new ListUrlFormProcessor() ) )
		        );
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	private static class RemoteEndpointLabelPrinter implements Printer<WebCmsRemoteEndpoint>
	{
		private final EntityRegistry entityRegistry;

		@Override
		public String print( WebCmsRemoteEndpoint endpoint, Locale locale ) {
			EntityConfiguration<WebCmsRemoteEndpoint> entityConfiguration = entityRegistry.getEntityConfiguration( WebCmsRemoteEndpoint.class );
			String name = entityConfiguration.getEntityMessageCodeResolver().getNameSingular( locale );

			return name + ": " + endpoint.getTargetUrl();
		}
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	private static class UrlsControlViewElementBuilder implements ViewElementBuilder<ContainerViewElement>
	{
		private final EntityViewElementBuilderHelper builderHelper;

		@Override
		public ContainerViewElement build( ViewElementBuilderContext builderContext ) {
			val entityViewRequest = builderContext.getAttribute( EntityViewRequest.class );
			val entityViewContext = entityViewRequest.getEntityViewContext();
			val endpoint = entityViewRequest.getCommand().getEntity( WebCmsRemoteEndpoint.class );
			val association = entityViewContext.getEntityConfiguration().association( "webCmsRemoteEndpoint.urls" );

			EntityMessageCodeResolver codeResolver = association.getAttribute( EntityMessageCodeResolver.class );

			SortableTableBuilder tableBuilder = builderHelper.createSortableTableBuilder( WebCmsUrl.class );
			tableBuilder.properties( EntityPropertySelector.of( "path", "httpStatus" ) );
			tableBuilder.noSorting();
			tableBuilder.tableName( "urls" );

			List<WebCmsUrl> urls = new ArrayList<>( endpoint.getUrls() );
			urls.sort( Comparator.comparing( WebCmsUrl::getPath ) );

			tableBuilder.items( EntityUtils.asPage( urls ) );
			tableBuilder.tableOnly();
			tableBuilder.hideResultNumber();
			tableBuilder.noResults( html.builders.container() );

			val associationLinkBuilder = entityViewContext.getLinkBuilder()
			                                              .forInstance( entityViewContext.getEntity() )
			                                              .association( "webCmsRemoteEndpoint.urls" );
			EntityListActionsProcessor actionsProcessor = new EntityListActionsProcessor(
					association.getTargetEntityConfiguration(),
					associationLinkBuilder,
					new EntityMessages( codeResolver )
			);
			tableBuilder.headerRowProcessor( actionsProcessor );
			tableBuilder.valueRowProcessor( actionsProcessor );

			String message = entityViewContext.getMessageCodeResolver()
			                                  .getMessage( "properties.urls.add", "Add a path to redirect" );

			return html.builders.div()
			                    .name( "formGroup-urls" )
			                    .add( tableBuilder )
			                    .add( bootstrap.builders.button()
			                                            .link( associationLinkBuilder.createView().toUriString() )
			                                            .style( Style.PRIMARY )
			                                            .text( message ) )
			                    .build( new DefaultViewElementBuilderContext() );
		}
	}

	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	private static class UpdateRedirectFormProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			ContainerViewElementUtils.move( container, "buttons", SingleEntityFormViewProcessor.LEFT_COLUMN );
			ContainerViewElementUtils.move( container, "formGroup-urls", SingleEntityFormViewProcessor.RIGHT_COLUMN );
		}
	}

	private static class ListUrlFormProcessor extends SimpleEntityViewProcessorAdapter
	{
		@Override
		public void doControl( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
			val endpointContext = entityViewRequest.getEntityViewContext().getParentContext();
			entityView.setRedirectUrl( endpointContext.getLinkBuilder().forInstance( endpointContext.getEntity() ).updateView().toUriString() );
		}
	}

	private static class CreateUrlFormProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void doGet( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command ) {
			command.getEntity( WebCmsUrl.class ).setHttpStatus( HttpStatus.MOVED_PERMANENTLY );
		}

		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			String message = entityViewRequest.getEntityViewContext().getMessageCodeResolver()
			                                  .getMessage( "bodyTitle.create", "Add a path to redirect" );

			container.addFirstChild( new NodeViewElementBuilder( "h4" )
					                         .add( TextViewElement.html( message ) ).build( builderContext ) );
		}
	}

	private static class UpdateUrlFormProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			String message = entityViewRequest.getEntityViewContext().getMessageCodeResolver()
			                                  .getMessage( "bodyTitle.update", "Update redirect path" );

			container.addFirstChild( new NodeViewElementBuilder( "h4" )
					                         .add( TextViewElement.html( message ) ).build( builderContext ) );
		}
	}
}
